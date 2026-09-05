package dev.withershop.commands;

import dev.withershop.points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**

* /points
* /points <player>
* /points give <player> <amount>
* /points set <player> <amount>
  */
  public class PointsCommand implements CommandExecutor {

  private final PointsManager pointsManager;

  public PointsCommand(PointsManager pointsManager) {
  this.pointsManager = pointsManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

   // /points
   if (args.length == 0) {
       return handleCheckOwnBalance(sender);
   }

   // /points give <player> <amount>
   if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
       return handleGive(sender, args[1], args[2]);
   }

   // /points set <player> <amount>
   if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
       return handleSet(sender, args[1], args[2]);
   }

   // /points <player>
   if (args.length == 1) {
       return handleCheckOtherBalance(sender, args[0]);
   }

   sender.sendMessage(
           Component.text(
                   "Usage: /points | /points <player> | /points give <player> <amount> | /points set <player> <amount>",
                   NamedTextColor.RED
           )
   );

   return true;

  }

  private boolean handleCheckOwnBalance(CommandSender sender) {

   if (!(sender instanceof Player player)) {
       sender.sendMessage("Only players have points.");
       return true;
   }

   int total = pointsManager.getPoints(player.getUniqueId());

   player.sendMessage(
           Component.text("☠ ", NamedTextColor.DARK_GRAY)
                   .append(Component.text(
                           "Wither Points: ",
                           NamedTextColor.GRAY
                   ))
                   .append(
                           Component.text(
                                   total,
                                   NamedTextColor.LIGHT_PURPLE
                           ).decorate(TextDecoration.BOLD)
                   )
   );

   return true;

  }

  @SuppressWarnings("deprecation")
  private boolean handleCheckOtherBalance(
  CommandSender sender,
  String targetName
  ) {


   OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

   if (!target.hasPlayedBefore() && !target.isOnline()) {
       sender.sendMessage(
               Component.text(
                       "Player '" + targetName + "' was not found.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   int total = pointsManager.getPoints(target.getUniqueId());

   sender.sendMessage(
           Component.text("☠ ", NamedTextColor.DARK_GRAY)
                   .append(Component.text(
                           targetName + "'s Wither Points: ",
                           NamedTextColor.GRAY
                   ))
                   .append(
                           Component.text(
                                   total,
                                   NamedTextColor.LIGHT_PURPLE
                           ).decorate(TextDecoration.BOLD)
                   )
   );

   return true;


  }

  @SuppressWarnings("deprecation")
  private boolean handleGive(
  CommandSender sender,
  String targetName,
  String amountArg
  ) {

   if (!sender.isOp()) {
       sender.sendMessage(
               Component.text(
                       "Only OPs can give points.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   int amount;

   try {
       amount = Integer.parseInt(amountArg);
   } catch (NumberFormatException e) {
       sender.sendMessage(
               Component.text(
                       "Amount must be a whole number.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   if (amount <= 0) {
       sender.sendMessage(
               Component.text(
                       "Amount must be positive.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

   if (!target.hasPlayedBefore() && !target.isOnline()) {
       sender.sendMessage(
               Component.text(
                       "Player '" + targetName + "' was not found.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   pointsManager.addPoints(target.getUniqueId(), amount);

   int newTotal = pointsManager.getPoints(target.getUniqueId());

   sender.sendMessage(
           Component.text("✔ ", NamedTextColor.GREEN)
                   .decorate(TextDecoration.BOLD)
                   .append(
                           Component.text(
                                   "Gave " + amount
                                           + " points to "
                                           + targetName
                                           + " (new total: "
                                           + newTotal
                                           + ").",
                                   NamedTextColor.GREEN
                           )
                   )
   );

   if (target.getPlayer() != null) {
       target.getPlayer().sendMessage(
               Component.text("☠ ", NamedTextColor.DARK_GRAY)
                       .append(
                               Component.text(
                                       "An admin gave you "
                                               + amount
                                               + " Wither points! (new total: "
                                               + newTotal
                                               + ")",
                                       NamedTextColor.GREEN
                               )
                       )
       );
   }

   return true;

  }

  @SuppressWarnings("deprecation")
  private boolean handleSet(
  CommandSender sender,
  String targetName,
  String amountArg
  ) {

   if (!sender.isOp()) {
       sender.sendMessage(
               Component.text(
                       "Only OPs can set points.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   int amount;

   try {
       amount = Integer.parseInt(amountArg);
   } catch (NumberFormatException e) {
       sender.sendMessage(
               Component.text(
                       "Amount must be a whole number.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   if (amount < 0) {
       sender.sendMessage(
               Component.text(
                       "Amount cannot be negative.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

   if (!target.hasPlayedBefore() && !target.isOnline()) {
       sender.sendMessage(
               Component.text(
                       "Player '" + targetName + "' was not found.",
                       NamedTextColor.RED
               )
       );
       return true;
   }

   pointsManager.setPoints(target.getUniqueId(), amount);

   sender.sendMessage(
           Component.text("✔ ", NamedTextColor.GREEN)
                   .decorate(TextDecoration.BOLD)
                   .append(
                           Component.text(
                                   "Set " + targetName
                                           + "'s points to "
                                           + amount
                                           + ".",
                                   NamedTextColor.GREEN
                           )
                   )
   );

   if (target.getPlayer() != null) {
       target.getPlayer().sendMessage(
               Component.text(
                       "Your Wither Points have been set to "
                               + amount
                               + " by an admin.",
                       NamedTextColor.YELLOW
               )
       );
   }

   return true;

  }
  }
