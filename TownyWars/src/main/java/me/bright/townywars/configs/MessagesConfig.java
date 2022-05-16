package me.bright.townywars.configs;

import me.bright.townywars.TownyWars;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessagesConfig extends WConfig{

    private FileConfiguration conf;
    private File file;

    public MessagesConfig() {
        initialize();
    }

    private void initialize() {
        file = new File(TownyWars.getPlugin().getDataFolder() + "/messages.yml");
        fileMaker(file);
        conf = YamlConfiguration.loadConfiguration(file);
        values();
        save();
    }

    private void dataConfig(String path, Object value) {
        if(conf.get(path) == null) {
            conf.set(path, value);
        }
    }

    private void values() {
        dataConfig("messages.prefix","&cWars > &f");
        dataConfig("messages.havenot_town","&cВы не состоите в городе");
        dataConfig("messages.not_mayor","&cВы не являетесь мэром города для выполнения этого действия");
        dataConfig("messages.town_not_exist","&cТакого города не существует");
        dataConfig("messages.war_declare","&aГород &c[attacker] &aобъявил войну городу &c[opponent]");
        dataConfig("messages.your_town","&cВы не можете объявить войну своему городу");
        dataConfig("messages.opponent_in_war","&cДанный город уже ведет войну");
        dataConfig("messages.you_in_war","&cВаш город уже ведет войну");
        dataConfig("messages.cant_start_war","&cВы не можете начать активную фазу боя");
        dataConfig("messages.start_war","&cВы начали фазу активного боя");
        dataConfig("messages.attacker_low_online","&cУ вас недостаточно онлайна в городе для начала войны, должно быть не менее 25%");
        dataConfig("messages.opponent_low_online","&cУ оппонента недостаточно онлайна в городе дня начала войны, должно быть не менее 25%");
        dataConfig("messages.opponent_mayor_offline","&cМайор вражеского города не в сети, вы не можете начать войну");
        dataConfig("messages.dont_use_command_in_war","&cВы не можете использовать данную команду во время войны");
        dataConfig("messages.error","&cПроизошла ошибка");
        dataConfig("messages.accept_invite","&aВы приняли приглашение на войну");
        dataConfig("messages.deny_invite","&cВы отклонили приглашение на войну");
        dataConfig("messages.you_not_in_war","&cВы не ведете войну");
        dataConfig("messages.invite_sent","&cВы отправили приглашение на войну городу &7[town]");
        dataConfig("messages.not_nation","&cВы не состоите в нации");
        dataConfig("messages.town_not_capital_nation","&cВаш город не является столицей нации");
        dataConfig("messages.not_general_town","&cПризывать города нации могут только главные города войны");
        dataConfig("messages.nation_towns_invited","&cВы призвали все города нации");
        dataConfig("messages.nation_not_alliances","&cУ нации нет альянсов");
        dataConfig("messages.nation_alliances_invited","&cВы призвали столицы наций в альянсе");
        dataConfig("messages.cant_leave_in_warphase","&cВы не можете покинуть войну во время активной фазы");
        dataConfig("messages.cant_leave_as_general_town","&cВы не можете покинуть войну, так как вы являетесь главным городом");
        dataConfig("messages.leave_war","&aВы успешно покинули войну");
        dataConfig("messages.cant_set_flag","&cВы не можете установить флаг здесь");
        dataConfig("messages.not_enough_money","&cУ вас не хватает $ (нужно 20)");
        dataConfig("messages.end_war_message","&aВойна [attacker] ⚔ [opponent] закончилась. Победитель: [winner]");
        dataConfig("messages.destroy_town","&cГород [town] был уничтожен!");
        dataConfig("messages.merge_town","&cГород [loser] был объединен с городом [winner]!");
        dataConfig("messages.town_too_far","&cГород находится слишком далеко для объединения");
        dataConfig("messages.town_spared","&aГород [town] был пощажён");
        dataConfig("messages.town_enslaved","&cГород [loser] стал марионеткой города [winner]!");
        dataConfig("messages.dont_use_command_in_preparation","&cВы не можете использовать данную команду во время фазы ожидания");
        dataConfig("messages.havent_enslaved_towns","&cУ вас нет марионеток");
        dataConfig("messages.not_enslaved_town","&cВы не можете управлять этим городом");
        dataConfig("messages.cant_leave_nation","&cВы не можете выйти из нации");
        dataConfig("messages.war_end_force","&cВойна [attacker] VS [opponent] принудительно завершилась");
        dataConfig("messages.war_end_force_win_attacker","&cВойна [attacker] VS [opponent] принудительно завершилась, победитель: [attacker]");
        dataConfig("messages.alliance_invite_accepted","&aВаш запрос на создание альянса принят.");
        dataConfig("messages.you_accept_alliance_invite","&aВы приняли запрос на вступлвние в альянс");
        dataConfig("messages.you_deny_alliance_invite","&cВы отклонили запрос на вступление с альянс");
        dataConfig("messages.alliance_invite_denied","&cВаш запрос на создание альянса отклонен");
        dataConfig("messages.nation_not_exists","&cНации с таким именем не существует");
        dataConfig("messages.nation_has_invite","&cНация уже имеет приглашение на вступление в альянс");
        dataConfig("messages.not_king_of_nation","&cВы не глава нации");
        dataConfig("messages.king_of_nation_offline","&cГлава нации не в сети");
        dataConfig("messages.invite_send_1","&aНация [nationName] предлагает создать альянс с именем [allyName]");
        dataConfig("messages.invite_send_11","&aНация [nationName] предлагает вступить вам в альянс с именем [allyName]");
        dataConfig("messages.invite_send_2","&aУ вас есть &c3 &aминуты на принятие или отклонение приглашения");
        dataConfig("messages.invite_send_3","&a/alliance query accept &7 - принять приглашение");
        dataConfig("messages.invite_send_4","&c/alliance query deny &7 - отклонить приглашение");
        dataConfig("messages.you_sent_alliance_invite","&aВы отправили запрос");
        dataConfig("messages.you_havent_alliance_invites","&cУ вас нет действующих запросов на вступление в альянс");
        dataConfig("messages.not_alliance","&cВы не состоите в альянсе");
        dataConfig("messages.not_head_of_alliance","&cВаша нация не является главой альянса");
        dataConfig("messages.nation_not_in_alliance","&cДанная нация не является членом вашего альянса");
        dataConfig("messages.you_set_head_nation","&aВы передали руководство альянсом нации [nationName]");
        dataConfig("messages.nation_already_in_your_alliance","&cНация уже состоит в вашем альянсе");
        dataConfig("messages.nation_already_in_alliance","&cНация уже состоит в альянсе");
        dataConfig("messages.you_kick_nation_out_of_alliance","&cВы исключили нацию [nationName] из альянса");
        dataConfig("messages.you_head_of_alliance","&cВы не можете покинуть альянс, так как являетесь его главой");
        dataConfig("messages.you_left_in_alliance","&aВы успешно покинули альянс");
        dataConfig("messages.you_in_alliance","&cВы уже состоите в альянсе");
    }


    public FileConfiguration getConfig() {
        return conf;
    }

    public void save() {
        try {
            conf.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
