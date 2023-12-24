package legacystore.store.legacytnt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import javax.swing.plaf.basic.BasicIconFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public final class LegacyTNT extends JavaPlugin implements Listener {

    private String botaoparaexecutar;
    private Map<String, TipoTNT> tiposTNT;
    private boolean animacao;
    private int tntsNaAnimacao;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getServer().getConsoleSender().sendMessage("§bPlugin ligado!");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("givetnt").setExecutor(new ComandoReceberTNT(this));

        botaoparaexecutar = getConfig().getString("opcoes.botaoparaexecutar", "RIGHT").toUpperCase();

        Map<String, TipoTNT> tiposTNT = new HashMap<>();
        for (String tipoTNT : getConfig().getConfigurationSection("tnts").getKeys(false)) {
            boolean animacao = getConfig().getBoolean("tnts." + tipoTNT + ".animacao", true);
            int tntsNaAnimacao = getConfig().getInt("tnts." + tipoTNT + ".tntsnaanimacao", 6);
            TipoTNT tipoTNTConfig = new TipoTNT(animacao, tntsNaAnimacao);
            tiposTNT.put(tipoTNT, tipoTNTConfig);
        }
        setTiposTNT(tiposTNT);
    }

    public TipoTNT getTipoTNT(String tipoTNT) {
        return tiposTNT.get(tipoTNT);
    }

    private void setTiposTNT(Map<String, TipoTNT> tiposTNT) {
        this.tiposTNT = tiposTNT;
    }


    @EventHandler
    public void Interacaodojogador(PlayerInteractEvent event) {
        Player jogador = event.getPlayer();
        ItemStack item = jogador.getItemInHand();

        if (event.getAction().toString().contains(botaoparaexecutar) && item != null && item.getType() == Material.TNT) {
            Block targetBlock = jogador.getTargetBlock((HashSet<Byte>) null, 6);
            if (targetBlock != null) {
                String tipoTNTConfigurada = getTipoTNTConfigurada(item);

                if (tipoTNTConfigurada != null) {
                    TipoTNT tipoTNT = getTipoTNT(tipoTNTConfigurada);

                    if (tipoTNT != null) {
                        if (tipoTNT.isAnimacao()) {
                            animateTNT(jogador, targetBlock.getLocation().add(0.5, 1, 0.5), tipoTNT);
                            jogador.getWorld().spawnEntity(targetBlock.getLocation().add(0.5, 1, 0.5), EntityType.PRIMED_TNT);

                        } else {
                            // Lança a TNT imediatamente se a animação estiver desativada
                            jogador.getWorld().spawnEntity(targetBlock.getLocation().add(0.5, 1, 0.5), EntityType.PRIMED_TNT);
                        }

                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            jogador.getInventory().removeItem(item);
                        }
                    }
                }
            }
        }
    }

    private String getTipoTNTConfigurada(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            for (String tipoTNT : getConfig().getConfigurationSection("tnts").getKeys(false)) {
                String nomeConfigurado = getConfig().getString("tnts." + tipoTNT + ".item.nome");
                if (displayName.equals(nomeConfigurado)) {
                    return tipoTNT;
                }
            }
        }
        return null;
    }


    private void animateTNT(Player player, Location targetLocation, TipoTNT tipoTNT) {
        if (!tipoTNT.isAnimacao()) {
            // Caso a animação esteja desativada, apenas joga a TNT para longe e explode
            Location startLocation = player.getLocation().add(0, 1.5, 0);
            Vector direction = targetLocation.toVector().subtract(startLocation.toVector()).normalize();
            startLocation.add(direction.multiply(0.5)); // Ajuste a distância conforme necessário

            TNTPrimed tnt = startLocation.getWorld().spawn(startLocation, TNTPrimed.class);
            tnt.setVelocity(direction.multiply(2.0)); // Ajuste a velocidade conforme necessário

            return; // Encerra a função, pois não é necessário continuar a animação
        }

        // Continua com a animação visual + animação para longe
        new BukkitRunnable() {
            private final int totalFrames = 20;
            private int currentFrame = 0;

            @Override
            public void run() {
                if (currentFrame >= totalFrames) {
                    cancel();
                    return;
                }

                double progress = (double) currentFrame / totalFrames;
                Location currentLocation = player.getLocation().add(0, 1.5, 0);
                Vector direction = targetLocation.toVector().subtract(currentLocation.toVector()).normalize();
                currentLocation.add(direction.multiply(progress * tipoTNT.getTntsNaAnimacao()));

                Item visualTNT = player.getWorld().dropItem(currentLocation, new ItemStack(Material.TNT));
                visualTNT.setPickupDelay(Integer.MAX_VALUE);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        visualTNT.remove();
                    }
                }.runTaskLater(LegacyTNT.this, 20L);

                currentFrame++;
            }
        }.runTaskTimer(this, 0, 1);
    }


    public void setAnimacao(boolean animacao) {
        this.animacao = animacao;
    }

    public void setTntsNaAnimacao(int tntsNaAnimacao) {
        this.tntsNaAnimacao = tntsNaAnimacao;
    }

}
