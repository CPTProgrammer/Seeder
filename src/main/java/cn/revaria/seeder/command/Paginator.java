package cn.revaria.seeder.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Paginator {
    private MutableText[] entryList;
    private final String modId;
    private final Text title;
    private final String pageCommand;

    private final int entriesPerPage;

    Paginator(String modId, Text title, int entriesPerPage, String pageCommand, MutableText[] entryList) {
        this.modId = modId;
        this.title = title;
        this.entriesPerPage = entriesPerPage;
        this.pageCommand = pageCommand;

        this.entryList = entryList;
    }

    public int getPageCount() {
        return entryList.length / entriesPerPage + (entryList.length % entriesPerPage > 0 ? 1 : 0);
    }

    public void print(ServerCommandSource source, int page) {
        printLine(source, Text.literal("").styled(style -> style.withColor(Formatting.DARK_GREEN)).append(Text.literal("=== ")).append(this.title).append(Text.literal(" ===")));
        int start = (page - 1) * entriesPerPage;
        int end = page * entriesPerPage;
        for (int i = start; i < end; i++) {
            printLine(source, i >= entryList.length ? Text.literal("") : entryList[i]);
        }
        printLine(source, Text.literal("").styled(style -> style.withColor(Formatting.DARK_GREEN))
            .append(Text.literal("<< ").styled(style -> page == 1 ? style.withColor(Formatting.DARK_GRAY) : style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("seeder.paginator.previous_page"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, this.pageCommand + (page - 1)))))
            .append(Text.literal("|"))
            .append(Text.literal(" >>").styled(style -> page == getPageCount() ? style.withColor(Formatting.DARK_GRAY) : style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("seeder.paginator.next_page"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, this.pageCommand + (page + 1)))))
            .append(Text.literal("  "))
            .append(Text.literal(String.valueOf(page))).append(Text.literal("/")).append(Text.literal(String.valueOf(getPageCount())))
        );
    }

    private void printLine(ServerCommandSource source, MutableText content) {
        MutableText prefix = Text.literal("[").styled(style -> style.withColor(Formatting.DARK_GREEN).withBold(true))
            .append(Text.literal(modId).styled(style -> style.withColor(Formatting.GREEN).withBold(false)))
            .append(Text.literal("] ").styled(style -> style.withColor(Formatting.DARK_GREEN).withBold(true)));
        source.sendFeedback(() -> Text.literal("").append(prefix).append(content), false);
    }
}
