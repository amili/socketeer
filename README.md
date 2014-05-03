socketeer
=========

A proxy with a twist.
Written in Java, since this framework has many features built in, native proxy support (enables chaining),
many available libraries and is not so patent-encumeberd as i.e. .NET framework.
Also, it is very portable framework, being also a core of Android operating system.

This is a project born out of the need to make a different and specific type of VPN.
It is an application layer (according to OSI models) multiple topology capable proxy. 

The general idea is to make a simple firewall "go-through" (mainly for testing, the author needed it many times in the past),
without having to modify existing system (no TUN/TAP VPN adapter fiddeling). A Socksifier program can make almost any non-SOCKS aware
software SOCKS aware. 

It currently uses a STOMP tunneling, but is meant to use more than that.

Currently alpha quality (not production ready).

Concept
-------
The idea is to have a "back-to-back" proxy.
Any side sees the other side as its network space extension.
The concept of "sourcesinks", endpoints which can act as a source or a sink, has been devised.
Simplest configuration is to have one source and one sink, which are located ideally on different computers.
Both can also be on the same physical/virtual computer.

The connection is relayed by some means. Initial idea was through STOMP, since a small library existed (Gozirra).
Generic plugin support enables usage of many more topologies, like i.e. IRC, XMPP, some kind of a JDBC bridge towards a common database.
Java has the also the excellent jgroups library (which natevily supports STOMP frontend), and also many other message broker implementations.

The final idea is to support one day also Apache Camel (via a plugin), which would make many more interconnecting possibilities a breeze.

Protocol support
---------------
Has frontends:
- fixed redirection to a host/port, specified in the configuration
- SOCKS4(a)/SOCKS5
- HTTP HEAD, GET, POST and CONNECT method (to be used also as HTTP(s) proxy)
- content based redirection (modelled after another project "sf-rmr")

GUI
---
No GUI yet.
