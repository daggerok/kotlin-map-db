package daggerok

import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

fun main() {
  // val dbFilePath = Paths.get(".", "build", UUID.randomUUID().toString(), "my.db").normalize().toAbsolutePath().toFile()
  val dbFilePath = Paths.get(".", "build", "my.db").normalize().toAbsolutePath().toFile()
  println("making sure $dbFilePath is exist")
  Files.createDirectories(Paths.get(dbFilePath.parent))

  val db = DBMaker.fileDB(dbFilePath)
      .cleanerHackEnable()
      .closeOnJvmShutdown()
      .closeOnJvmShutdownWeakReference()
      .executorEnable()
      .fileChannelEnable()
      .fileLockDisable()
      .fileLockWait(TimeUnit.SECONDS.toMillis(5))
      .fileMmapEnableIfSupported()
      .make()

  val map = db.hashMap("foo", Serializer.STRING, Serializer.STRING).createOrOpen()
  print("add in newly created: $map: ${map.keys}")
  map["ololo"] = "ololo"
  println(" -> ${map.keys}")

  val foo = db.hashMap("foo", Serializer.STRING, Serializer.STRING).createOrOpen()
  print("add in existing map: $foo: ${foo.keys}")
  foo["trololo"] = "trololo"
  println(" -> ${foo.keys}")

  db.close()
}
