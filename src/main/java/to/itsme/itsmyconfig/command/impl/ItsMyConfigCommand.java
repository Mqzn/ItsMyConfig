package to.itsme.itsmyconfig.command.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.help.CommandHelp;
import to.itsme.itsmyconfig.ItsMyConfig;
import to.itsme.itsmyconfig.config.placeholder.PlaceholderType;
import to.itsme.itsmyconfig.util.Message;
import to.itsme.itsmyconfig.util.Utilities;

import java.util.Arrays;
import java.util.Collections;

@Command("itsmyconfig")
public final class ItsMyConfigCommand {

    private final ItsMyConfig plugin;

    public ItsMyConfigCommand(final ItsMyConfig plugin) {
        this.plugin = plugin;
    }

    @DefaultFor("~")
    public void usage(
            final BukkitCommandActor actor,
            final CommandHelp<String> help
    ) {
        final StringBuilder builder = new StringBuilder();
        final String hyphen = "\n<gray><strikethrough>-----------------------------------------</strikethrough></gray>";
        builder.append(hyphen)
                .append('\n')
                .append("<white>This server is running <aqua><plugin></aqua> by <gold><author></gold></white>")
                .append('\n');

        Collections.reverse(help);
        for (final String line : help) {
            builder.append('\n').append(line);
        }

        builder.append(hyphen);
        actor.reply(Utilities.MM.deserialize(builder.toString(), pluginInfo(), authorInfo()));
    }

    private TagResolver pluginInfo() {
        final PluginDescriptionFile description = plugin.getDescription();
        return TagResolver.resolver("plugin", (argumentQueue, context) -> Tag.selfClosingInserting(
                Component.text().content(description.getName())
                        .hoverEvent(
                                Utilities.MM.deserialize(
                                        Utilities.toString(
                                                Arrays.asList(
                                                        " ",
                                                        "<white>Name: <aqua>" + description.getName(),
                                                        "<white>Version: <aqua>" + description.getVersion(),
                                                        "<white>Support Server: <aqua>" + description.getWebsite(),
                                                        " "
                                                )
                                        )
                                )
                        )
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, description.getWebsite()))
        ));
    }

    private TagResolver authorInfo() {
        return TagResolver.resolver("author", (argumentQueue, context) -> Tag.selfClosingInserting(
                Component.text().content("iiAhmedYT")
                        .hoverEvent(
                                Utilities.MM.deserialize(
                                        Utilities.toString(
                                                Arrays.asList(
                                                        " ",
                                                        "<white>Discord: <aqua>@iiAhmedYT</aqua>",
                                                        "<white>GitHib: <aqua>https://github.com/iiAhmedYT</aqua>",
                                                        " "
                                                )
                                        )
                                )
                        )
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/iiAhmedYT"))
        ));
    }

    @Subcommand("reload")
    @CommandPermission("itsmyconfig.reload")
    @Description("Reloads the plugin config")
    public void reload(final BukkitCommandActor actor) {
        plugin.loadConfig();
        Message.RELOAD.send(actor);
    }

    @Subcommand("message")
    @CommandPermission("itsmyconfig.message")
    @Description("Sends messages to players")
    public void message(
            final BukkitCommandActor actor,
            @Named("target") final EntitySelector<Player> players,
            @Named("message") final String message
    ) {
        players.forEach(player -> {
            final String[] strings = message.split("\\\\r?\\\\n|\\\\r");
            for (final String string : strings) {
                player.sendMessage(this.plugin.getSymbolPrefix() + string);
            }
        });

        if (actor.isPlayer()) {
            Message.MESSAGE_SENT.send(actor);
        }
    }

    @Subcommand("config")
    @CommandPermission("itsmyconfig.config")
    @Description("Sets config values for placeholder")
    public void config(
            final BukkitCommandActor actor,
            @Named("placeholder") final String placeholder,
            @Named("value") final String value
    ) {
        final FileConfiguration config = plugin.getConfig();
        final ConfigurationSection section = config.getConfigurationSection("custom-placeholder." + placeholder);
        if (section == null) {
            actor.reply(Utilities.MM.deserialize("<red>Placeholder <yellow>" + placeholder + "</yellow> was not found.</red>"));
            return;
        }

        final PlaceholderType type = PlaceholderType.find(section.getString("type"));
        if (type == PlaceholderType.ANIMATION || type == PlaceholderType.RANDOM) {
            actor.reply(Utilities.MM.deserialize("<red>Placeholder <yellow>" + placeholder + "</yellow>'s type is not supported via commands.</red>"));
            return;
        }

        section.set("value", value);
        plugin.saveConfig();
        plugin.loadConfig();
        actor.reply(Utilities.MM.deserialize("<green>Placeholder <yellow>" + placeholder + "</yellow>'s value was updated successfully!</green>"));
    }

    @Command("message")
    @CommandPermission("itsmyconfig.message")
    public void msgCommand(
            final BukkitCommandActor actor,
            @Named("target") final EntitySelector<Player> players,
            @Named("message") final String message
    ) {
        this.message(actor, players, message);
    }

    @Command("config")
    @CommandPermission("itsmyconfig.config")
    public void configCommand(
            final BukkitCommandActor actor,
            @Named("placeholder") final String placeholder,
            @Named("value") final String value
    ) {
        this.config(actor, placeholder, value);
    }

}
