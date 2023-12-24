package legacystore.store.legacytnt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ComandoReceberTNT implements CommandExecutor {

    private final LegacyTNT plugin;

    public ComandoReceberTNT(LegacyTNT plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfig().getString("mensagens.permissao"));
            return true;
        }

        Player jogador = (Player) sender;

        // Verifica se o jogador tem permissão para usar o comando
        if (!jogador.hasPermission("legacytnt.givetnt")) {
            jogador.sendMessage(plugin.getConfig().getString("mensagens.permissao"));
            return true;
        }

        // Verifica se o comando possui argumentos suficientes
        if (args.length < 3) {
            jogador.sendMessage(plugin.getConfig().getString("mensagens.uso_correto"));
            return true;
        }

        Player jogadorAlvo = Bukkit.getPlayer(args[0]);

        // Verifica se o jogador alvo é válido
        if (jogadorAlvo == null || !jogadorAlvo.isOnline()) {
            jogador.sendMessage(plugin.getConfig().getString("mensagens.jogador_nao_encontrado"));
            return true;
        }

        int quantidade;

        // Tenta converter a quantidade de TNT para um número
        try {
            quantidade = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            jogador.sendMessage(plugin.getConfig().getString("mensagens.quantidade_invalida"));
            return true;
        }

        // Garante que a quantidade não seja negativa
        if (quantidade <= 0) {
            jogador.sendMessage(plugin.getConfig().getString("mensagens.quantidade_invalida"));
            return true;
        }

        String tipoTNT = args[2];
        ItemStack tntItem = criarTNTModificada(tipoTNT, quantidade);

        jogadorAlvo.getInventory().addItem(tntItem);

        jogador.sendMessage(plugin.getConfig().getString("mensagens.entregue")
                .replace("%jogador%", jogadorAlvo.getName()));
        jogadorAlvo.sendMessage(plugin.getConfig().getString("mensagens.recebeu_jogador")
                .replace("%quantidade%", String.valueOf(quantidade)));

        return true;
    }

    private ItemStack criarTNTModificada(String tipoTNT, int quantidade) {
        // Obtém as configurações da TNT específica do tipoTNT no config.yml
        String caminho = "tnts." + tipoTNT.toLowerCase();
        boolean animacao = plugin.getConfig().getBoolean(caminho + ".animacao");
        int tntsNaAnimacao = plugin.getConfig().getInt(caminho + ".tntsnaanimacao");

        // Cria uma nova ItemStack com base nas configurações
        ItemStack tntItem = new ItemStack(Material.TNT, quantidade);
        ItemMeta meta = tntItem.getItemMeta();
        meta.setDisplayName(plugin.getConfig().getString(caminho + ".item.nome"));
        meta.setLore(plugin.getConfig().getStringList(caminho + ".item.lore"));


        if (plugin.getConfig().getBoolean(caminho + ".item.glow")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        tntItem.setItemMeta(meta);

        // Define as propriedades adicionais na classe LegacyTNT
        plugin.setAnimacao(animacao);
        plugin.setTntsNaAnimacao(tntsNaAnimacao);

        return tntItem;
    }
}
