package interium.fabric;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.util.*;
/** Local display replacement only; never alters server balances, ranks or scoreboard data. */
public final class DeltaBoardSpoofer {
 public static final String[] RANKS={"Игрок","Барон","Страж","Герой","Аспид","Сквид","Глава","Элита","Титан","Принц","Князь","Герцог"};
 public static Text replace(Text text){if(!DeltaVisualSettings.boardSpoofer)return text;Text result=text;if(DeltaVisualSettings.flag("board.rank",true)){String rank=RANKS[Math.round(DeltaVisualSettings.number("board.rankMode",0,0,11))];result=replace(result,"Ранг: ",rank,rankStyle(rank));}if(DeltaVisualSettings.flag("board.coins",true))result=replace(result,"Монет: ",number(DeltaVisualSettings.string("board.coinValue","0")),null);if(DeltaVisualSettings.flag("board.tokens",true))result=replace(result,"Токенов: ",number(DeltaVisualSettings.string("board.tokenValue","0")),null);return result;}
 private static String number(String value){String s=value.replace(",","").replace(" ","");if(!s.matches("[0-9]{1,60}"))return "0";return new java.math.BigInteger(s).toString();}
 private static Text replace(Text text,String label,String value,Style override){int index=text.getString().indexOf(label);if(index<0)return text;int cutoff=index+label.length();MutableText result=Text.empty();int[] copied={0};Style[] last={Style.EMPTY};text.visit((style,part)->{if(copied[0]>=cutoff)return Optional.of(true);int amount=Math.min(part.length(),cutoff-copied[0]);result.append(Text.literal(part.substring(0,amount)).setStyle(style));last[0]=style;copied[0]+=amount;return copied[0]>=cutoff?Optional.of(true):Optional.empty();},Style.EMPTY);result.append(Text.literal(value).setStyle(override==null?last[0]:override));return result;}
 private static Style rankStyle(String s){Formatting f=switch(s){case "Страж"->Formatting.YELLOW;case "Барон","Сквид"->Formatting.AQUA;case "Герой"->Formatting.GREEN;case "Аспид"->Formatting.DARK_AQUA;case "Глава","Титан"->Formatting.GOLD;case "Элита"->Formatting.DARK_PURPLE;case "Принц","Князь"->Formatting.RED;case "Герцог"->Formatting.DARK_RED;default->Formatting.WHITE;};return Style.EMPTY.withFormatting(f);}
}
