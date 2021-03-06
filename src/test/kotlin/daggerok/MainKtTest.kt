package daggerok

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

class MainKtTest {

  lateinit var db: DB

  @BeforeEach
  fun `before each`() {
    val f = Paths.get("build", UUID.randomUUID().toString(), ".db").toFile()
    f.parentFile.mkdirs()
    db = DBMaker.fileDB(f)
        .executorEnable()
        .fileLockDisable()
        .transactionEnable()
        .fileMmapEnableIfSupported()
        .closeOnJvmShutdown()
        .make()
  }

  @AfterEach
  fun `aster each`() {
    db.close()
  }

  @Test
  fun `test main`() {
    val map = db.hashMap("test")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.STRING)
        .createOrOpen()

    GlobalScope.launch {
      for (i in 1..1000) {
        map["$i-ololo"] = "$i-trololo"
        map["$i-trololo"] = "$i-ololo"
        db.commit()
      }
    }

    assertThat(map).hasSizeLessThan(2000)

    val defaultValue = "10"
    val sleep = System.getenv().getOrDefault("SLEEP", System.getProperty("sleep", defaultValue)).toLong()
    TimeUnit.SECONDS.sleep(sleep)

    assertThat(map).hasSize(2000)
  }
}
