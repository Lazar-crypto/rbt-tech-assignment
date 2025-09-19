import mu.KotlinLogging
import kotlin.jvm.java

val <T : Any> T.RBT_LOGGER get() = KotlinLogging.logger { this::class.java }
