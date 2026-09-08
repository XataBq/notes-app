package ru.xatab.avitonotes.data.remote

import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.xatab.avitonotes.data.remote.api.GigaApi
import ru.xatab.avitonotes.data.remote.api.OAuthApi
import ru.xatab.avitonotes.data.remote.api.SpeechApi
import ru.xatab.avitonotes.data.remote.auth.TokenProvider
import ru.xatab.avitonotes.data.remote.error.remoteCall
import ru.xatab.avitonotes.data.remote.repository.RemoteGigaChatRepository
import ru.xatab.avitonotes.data.remote.repository.RemoteSpeechRepository
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.model.TokenBalance

class RemoteIntegrationTest {
    private val server = MockWebServer()
    private lateinit var retrofit: Retrofit

    @Before fun setup() {
        server.start()
        retrofit = Retrofit.Builder().baseUrl(server.url("/"))
            .addConverterFactory(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }.asConverterFactory("application/json".toMediaType()),
            ).build()
    }

    @After fun teardown() = server.shutdown()
    private fun response(body: String, code: Int = 200) =
        server.enqueue(MockResponse().setResponseCode(code).setBody(body).setHeader("Content-Type", "application/json"))
    private fun oauth(token: String = "test-token") =
        response("""{"access_token":"$token","expires_at":4102444800000}""")
    private fun provider(key: String = "fixture-key") =
        TokenProvider(retrofit.create(OAuthApi::class.java), key, "GIGACHAT_API_PERS")
    private fun giga(tokens: TokenProvider = provider()) =
        RemoteGigaChatRepository(retrofit.create(GigaApi::class.java), tokens, "Return a concise task title")

    @Test fun `concurrent callers share one OAuth token and use form authentication`() = runBlocking {
        oauth()
        val tokens = provider()
        val values = coroutineScope { (1..12).map { async { tokens.token() } }.awaitAll() }
        assertEquals(setOf("test-token"), values.toSet())
        assertEquals(1, server.requestCount)
        val request = server.takeRequest()
        assertEquals("/api/v2/oauth", request.path)
        assertEquals("Basic fixture-key", request.getHeader("Authorization"))
        assertNotNull(UUID.fromString(request.getHeader("RqUID")))
        assertEquals("scope=GIGACHAT_API_PERS", request.body.readUtf8())
    }

    @Test fun `balance retries unauthorized once using a fresh token`() = runBlocking {
        oauth("old")
        response("{}", 401)
        oauth("new")
        response("""{"balance":[{"usage":"GigaChat","value":12345}],"extra":true}""")
        assertEquals(listOf(TokenBalance("GigaChat", 12345)), giga().balance())
        server.takeRequest()
        assertEquals("Bearer old", server.takeRequest().getHeader("Authorization"))
        server.takeRequest()
        val last = server.takeRequest()
        assertEquals("Bearer new", last.getHeader("Authorization"))
        assertEquals("/balance", last.path)
    }

    @Test fun `repeated unauthorized does not loop`() = runBlocking {
        oauth()
        response("{}", 401)
        oauth("new")
        response("{}", 401)
        assertEquals(FailureKind.AUTH, failure { giga().balance() })
        assertEquals(4, server.requestCount)
    }

    @Test fun `missing credentials fail without a network request`() = runBlocking {
        assertEquals(FailureKind.NOT_CONFIGURED, failure { giga(provider("")).balance() })
        assertEquals(0, server.requestCount)
    }

    @Test fun `malformed balance and rate limit are distinct failures`() = runBlocking {
        oauth()
        response("""{"unexpected":0}""")
        response("{}", 429)
        val repo = giga()
        assertEquals(FailureKind.INVALID_RESPONSE, failure { repo.balance() })
        assertEquals(FailureKind.RATE_LIMIT, failure { repo.balance() })
    }

    @Test fun `speech sends PCM bytes and combines recognition segments`() = runBlocking {
        oauth()
        response("""{"status":200,"result":["Купить","молоко"],"emotions":[]}""")
        val audio = ByteArray(6400) { (it % 127).toByte() }
        val repo = RemoteSpeechRepository(retrofit.create(SpeechApi::class.java), provider())
        assertEquals("Купить молоко", repo.recognize(audio))
        server.takeRequest()
        val request = server.takeRequest()
        assertEquals("/speech:recognize", request.path)
        assertEquals("audio/x-pcm;bit=16;rate=16000", request.getHeader("Content-Type"))
        assertArrayEquals(audio, request.body.readByteArray())
    }

    @Test fun `empty speech is rejected and chat normalizes only a valid response`() = runBlocking {
        oauth()
        response("""{"status":200,"result":[]}""")
        val tokens = provider()
        val speech = RemoteSpeechRepository(retrofit.create(SpeechApi::class.java), tokens)
        assertEquals(FailureKind.INVALID_RESPONSE, failure { speech.recognize(ByteArray(6400)) })
        response("""{"choices":[{"message":{"role":"assistant","content":" Купить молоко "}}]}""")
        assertEquals("Купить молоко", giga(tokens).makeTask("Завтра нужно купить молоко"))
        server.takeRequest()
        server.takeRequest()
        val request = server.takeRequest()
        assertEquals("/chat/completions", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("Завтра нужно купить молоко"))
        assertTrue(body.contains("\"role\":\"system\""))
    }

    @Test fun `cancellation remains cancellation`() = runBlocking {
        try {
            remoteCall<Unit> { throw CancellationException("test") }
            fail("Expected cancellation")
        } catch (
            _: CancellationException,
        ) { }
    }

    private suspend fun failure(block: suspend () -> Any): FailureKind {
        try {
            block()
            error("Expected AppException")
        } catch (error: AppException) {
            return error.kind
        }
    }
}
