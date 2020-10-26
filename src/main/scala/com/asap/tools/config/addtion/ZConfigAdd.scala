package com.asap.tools.config.addtion

import java.io.InputStreamReader

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.typesafe.TypesafeConfigSource.{fromDefaultLoader, fromTypesafeConfig}

object ZConfigAdd {
  def fromPropertiesFile[A](
                             filePath: String,
                             configDescriptor: ConfigDescriptor[A],
                             keyDelimiter: Option[Char] = None,
                             valueDelimiter: Option[Char] = None
                           )(implicit tag: Tag[A]): Layer[Throwable, ZConfig[A]] =
    fromConfigDescriptorM(
      readPropertiesFile(filePath, keyDelimiter, valueDelimiter)
        .map(configDescriptor from _)
    )

  def fromAppConf[A](configDescriptor: ConfigDescriptor[A],
                     keyDelimiter: Option[Char] = None,
                     valueDelimiter: Option[Char] = None
                    )(implicit tag: Tag[A]): Layer[Throwable, ZConfig[A]] =
    fromConfigDescriptorM {
      fromDefaultLoader match {
        case Left(value)   => ZIO.fail(value)
        case Right(source) => ZIO.effectTotal(configDescriptor from source)
      }
    }

  def fromConfFile[A](confFilePath: String,
                      configDescriptor: ConfigDescriptor[A]
                     )(implicit tag: Tag[A]): Layer[Throwable, ZConfig[A]] =
    fromConfigDescriptorM {
      readConfFile(confFilePath, configDescriptor).flatMap {
        case Left(value) => ZIO.fail(value)
        case Right(source) => ZIO.effectTotal(configDescriptor from source)
      }
    }

  private def fromConfigDescriptorM[R, E >: ReadError[K], A](configDescriptor: ZIO[R, E, ConfigDescriptor[A]])
                                                            (implicit tag: Tag[A]): ZLayer[R, E, ZConfig[A]] =
    ZLayer.fromEffect(
      configDescriptor.flatMap(
        descriptor => ZIO.fromEither(read(descriptor))
      )
    )
  private def readPropertiesFile[A](
                             filePath: String,
                             keyDelimiter: Option[Char] = None,
                             valueDelimiter: Option[Char] = None,
                             leafForSequence: LeafForSequence = LeafForSequence.Valid
                           ): Task[ConfigSource] =
    for {
      properties <- ZIO.bracket(
        ZIO.effect(getClass.getResourceAsStream(filePath))
      )(r => ZIO.effectTotal(r.close()))(inputStream => {
        ZIO.effect {
          val properties = new java.util.Properties()
          properties.load(inputStream)
          properties
        }
      })
    } yield ConfigSource.fromProperties(
      properties,
      filePath,
      keyDelimiter,
      valueDelimiter,
      leafForSequence
    )

  private def readConfFile[A](filePath: String, configDescriptor: ConfigDescriptor[A]): Task[Either[ReadError[String], ConfigSource]] =
    for {
      conf <- ZIO.bracket(
        ZIO.effect(getClass.getResource(filePath).openStream)
      )(r => ZIO.effectTotal(r.close()))(inputStream => {
        ZIO.effectTotal {
          ConfigFactory.parseReader(new InputStreamReader(inputStream))
        }
      })
    } yield fromTypesafeConfig(conf)
}
