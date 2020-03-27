package daggerok

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.nio.file.Paths
import java.util.*

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
        .closeOnJvmShutdownWeakReference()
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

    map["ololo"] = "trololo"
    map["trololo"] = "ololo"

    db.commit()
    assertThat(map).hasSize(2)
  }
}
