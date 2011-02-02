HttpProxyTunnel by Dave Landers <dave@srednal.com>

Java:
  Requires Java 6

Configuration:
  Extract httpproxytunnel.properties from the jar.
  Copy it to ~/.httpproxytunnel.properties (add the dot prefix).
  Edit to your needs.  Should be pretty self-explanatory.

Manual launching:
  java -jar httpproxytunnel.jar
  (developed and tested with Java 6)

Log files
  Default log configuration is in httpproxytunnel-log.properties from the jar.
  To override, extract it to ~/.httpproxytunnel-log.properties (again, add the dot).
  Configuration is java.util.logging.

MBeans
  You can connect to the mbean server using (for example) jconsole.
  You can query the configuration, and change a few things live.

Mac OS X launchd
  To launch the daemon when you login:
  Extract com.srednal.httpproxytunnel.plist from jar.
  Copy it to ~/Library/LaunchAgents/
  Copy the jar to  ~/Library/Application Support/httpproxytunnel/httpproxytunnel.jar
  Log out/in.