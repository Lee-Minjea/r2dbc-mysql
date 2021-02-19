package dev.miku.r2dbc.mysql.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * a utility that provides default eventloopgroup
 */
public class NettyEventLoopUtil {

    private EventLoopGroup defaultEventLoopGroup;
    private NettyEventLoopUtil() {
        if(Epoll.isAvailable()){
            defaultEventLoopGroup = new EpollEventLoopGroup(0,new DaemonThreadsFactory("r2dbc-mysql-netty") );
        } else {
            defaultEventLoopGroup = new NioEventLoopGroup(0,new DaemonThreadsFactory("r2dbc-mysql-netty") );
        }
    }

    private static class NettyEventLoopUtilHolder {
        static final NettyEventLoopUtil INSTANCE = new NettyEventLoopUtil();
    }

    public static EventLoopGroup getDefaultEventLoopGroup() {
        return NettyEventLoopUtilHolder.INSTANCE.defaultEventLoopGroup;
    }

}
