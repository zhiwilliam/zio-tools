package com.asap.tools.config

import zio._
import zio.config._
import ConfigDescriptor._
import com.asap.tools.config.addtion.ZConfigAdd

object Test extends App {
  import zio.{ ZIO, ZLayer }
  import zio.console._



  val configuration =
    (string("bridgeIp") |@| string("username"))(ApplicationConfig.apply, ApplicationConfig.unapply)

  val appConfig = nested("bridge")(configuration)






  case class ApplicationConfig(bridgeIp: String, userName: String)
  // 这个case class定义了你想从配置里面读取的值

  val businessLogic = for {
      appConfig <- getConfig[ApplicationConfig]
      _         <- putStrLn(appConfig.bridgeIp)
      _         <- putStrLn(appConfig.userName)
    } yield ()

    // 很简单的程序，程序定义了一个应用程序的设置，然后读出来打印
    // 但是这个程序的配置在哪里？以什么格式？怎么读？我现在不关心
    // 这一段相当于我的业务程序







  val propertiesLayer = ZConfigAdd.fromPropertiesFile("/test.properties", configuration)
  val appConfLayer = ZConfigAdd.fromAppConf(appConfig)
  val appConfFileLayer = ZConfigAdd.fromConfFile("/abc.conf", appConfig)

  // pgm1 为我的业务逻辑提供了从我指定的abc.conf里面去读配置的功能，还有就是打印在本机console的功能。
  val pgm1 = businessLogic.provideLayer(appConfFileLayer ++ Console.live)
  // pgm2 为我的业务逻辑提供了从经典的application.conf文件里面去读配置的功能。
  val pgm2 = businessLogic.provideLayer(appConfLayer ++ Console.live)
  // pgm3 为我的业务逻辑提供了从经典的test.properties文件里面去读配置的功能。
  val pgm3 = businessLogic.provideLayer(propertiesLayer ++ Console.live)
  // 大家可以看到我完全没有修改我的业务逻辑，通过装配不同的组件去实现不同的读配置方式。所有的错误处理，开文件
  // 关文件，文件格式等等繁杂的事情全部都隐藏了起来。不需要用户去操心。而且对主程序没有任何要求，不需要你去实现
  // 什么接口，不需要你做太多的特殊处理。你只需要定义一个case class说清楚你需要啥就好。
  // 注意哦，读app conf的API 和读properties的API 在底层是完全不同的API，我们通过自己写库，完全抹掉了它们的区别，
  // 做到了对用户完全透明。

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (pgm1 >>> pgm2 >>> pgm3).exitCode
}
