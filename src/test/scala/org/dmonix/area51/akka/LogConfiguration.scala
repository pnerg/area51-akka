package org.dmonix.area51.akka

import java.io.ByteArrayInputStream
import java.util.Locale

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

import org.apache.log4j.PropertyConfigurator

object LogConfiguration {
  def config: Config = config("on")
  def config(s: String):Config = ConfigFactory.parseString(
  s"""akka.loggers = [akka.testkit.TestEventListener] # makes both log-snooping and logging work
    |       akka.loglevel = "DEBUG"
    |       akka.actor.debug.receive = $s""".stripMargin)
}

/**
  * Trait to be added for Scala test cases in order to get log4j configured.
  * @author Peter Nerg
  */
trait LogConfiguration {
  // Configure language for proper logging outputs
  Locale.setDefault(Locale.US)
  System.setProperty("user.country", Locale.US.getCountry)
  System.setProperty("user.language", Locale.US.getLanguage)
  System.setProperty("user.variant", Locale.US.getVariant)

  PropertyConfigurator.configure(new ByteArrayInputStream(logCfg.getBytes))

  private def logCfg = """log4j.rootLogger=DEBUG, consoleAppender
                         |
                         |log4j.logger.com=INFO
                         |log4j.logger.org.dmonix=DEBUG
                         |log4j.logger.org.eclipse=WARN
                         |log4j.logger.org.apache=WARN
                         |              |
                         |log4j.appender.consoleAppender=org.apache.log4j.ConsoleAppender
                         |log4j.appender.consoleAppender.layout=org.apache.log4j.PatternLayout
                         |log4j.appender.consoleAppender.layout.ConversionPattern=%d [%15.15t] %-5p [%c] %m%n""".stripMargin

}