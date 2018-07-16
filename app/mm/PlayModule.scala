package mm

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

class PlayModule(env: Environment, config: Configuration)
  extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bind(classOf[MmConfig]).toInstance(MmConfig(config))

    bindActor[BotConversations]("bot-conversations")
    bindActorFactory[AddOkrsActor, AddOkrsActor.Factory]
    bind(classOf[MmWebSocket]).asEagerSingleton()
  }
}
