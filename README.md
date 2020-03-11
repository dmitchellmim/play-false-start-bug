# Sample project for intermittent TLS False Start failure

[Firefox enables TLS False Start](https://security.stackexchange.com/questions/184616/firefox-sending-application-data-in-middle-of-ssl-handshake), a feature in which the client may send the HTTP request in the middle of the TLS handshake. When Firefox issues a False Start, Play's Akka HTTP server seems to intermittently drop the HTTP request. When this happens, the connection remains open for 75 seconds until `akka.http.impl.engine.HttpIdleTimeoutException: HTTP idle-timeout encountered, no bytes passed in the last 75 seconds. This is configurable by akka.http.[server|client].idle-timeout`, the connection is reset, and Firefox tries again. Sometimes it works after the reset, but other times it takes two cycles (150 seconds), three cycles (225 seconds), or more.

I've reproduced the problem on Windows, Mac, and CentOS, both Play 2.6 and 2.7. [Running Play on Netty](https://www.playframework.com/documentation/2.7.x/NettyServer) doesn't seem to have the problem. It happens more frequently on my more complicated project, but still happens sometimes on this minimal example project. Which SSL certificate I use also seems to have an effect. I generated a self-signed certificate for this project, but it also occurs with an officially trusted certificate.

See the `Wireshark` directory for Wireshark output. The client sends the HTTP request via False Start at frame 37 among other places. The server immediately ACKs it and completes the TLS handshake (39: Change Cipher Spec and 41: Encrypted Handshake Message), but the server never responds with Application Data. There's a TCP keep-alive every 10 seconds until the connection is reset after 75 seconds (frame 69).

## Running

This project is based on the [Java hello world project](https://github.com/playframework/play-samples/tree/2.7.x/play-java-hello-world-tutorial) and has been modified to reproduce the problem (albeit inconsistently).

Run the project with:

```
sbt -Dhttps.port=9443 -Dplay.server.https.engineProvider="CustomSSLEngineProvider" run
```

Visit https://localhost:9443 in Firefox, clicking "Accept the Risk and Continue" at the warning. The page will occasionally take a long time—some multiple of 75 seconds—to load. You may need to refresh the page multiple times for the problem to occur. It sometimes takes me 5-10 tries with this example project, but happens almost every time with my more complicated project.