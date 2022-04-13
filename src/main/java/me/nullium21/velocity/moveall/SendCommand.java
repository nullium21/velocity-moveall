package me.nullium21.velocity.moveall;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SendCommand implements SimpleCommand {

    private ProxyServer server;

    public SendCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 2) {
            source.sendMessage(Component.text("Usage: /send <server> <player>"));
            return;
        }

        String where = args[0], whom = args[1];

        Stream<Player> players = whom.equalsIgnoreCase("all") ? server.getAllPlayers().stream() : server.getPlayer(whom).stream();

        Optional<RegisteredServer> srv = server.getServer(where);
        if (srv.isEmpty()) {
            source.sendMessage(Component.text("Server not found."));
        } else {
            players.forEach(p -> p.createConnectionRequest(srv.get()).fireAndForget());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        Stream<String> servers = server.getAllServers().stream().map(it -> it.getServerInfo().getName());
        Stream<String> players = server.getAllPlayers().stream().map(Player::getUsername);

        if (args.length == 0) return servers.collect(ImmutableList.toImmutableList());
        else if (args.length == 1) {
            return Stream.concat(players, Stream.of("all")).collect(ImmutableList.toImmutableList());
        }

        return ImmutableList.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().getPermissionValue("velocity.moveall.send") == Tristate.TRUE;
    }
}
