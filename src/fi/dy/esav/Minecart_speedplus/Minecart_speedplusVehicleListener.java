package fi.dy.esav.Minecart_speedplus;

import java.util.logging.Logger;

import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class Minecart_speedplusVehicleListener implements Listener {

	public static boolean isSign(Material m) {
		return Tag.SIGNS.isTagged(m);
	}

	int[] xmodifier = {-1, 0, 1};
	int[] ymodifier = {-2, -1, 0, 1, 2};
	int[] zmodifier = {-1, 0, 1};

	double line1;

	public static Minecart_speedplus plugin;
	Logger log = Logger.getLogger("Minecraft");

	boolean error;

	Vector flyingmod = new Vector(10, 0.01, 10);
	Vector noflyingmod = new Vector(1, 1, 1);

	public Minecart_speedplusVehicleListener(Minecart_speedplus instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVehicleCreate(VehicleCreateEvent event) {
		if (event.getVehicle() instanceof Minecart) {

			Minecart cart = (Minecart) event.getVehicle();
			cart.setMaxSpeed(0.4 * Minecart_speedplus.getSpeedMultiplier());

		}
	}

	/**
	 * Obtain sign information from a number
	 * @param x some number
	 * @return 1 if x is positive, -1 if negative, and 0 otherwise
	 */
	static int sign(double x){
		if(x>0.0)
			return 1;
		if(x<0.0)
			return -1;
		return 0;
	}

	/**
	 * Check if the minecart is moving in a straight direction
	 * @param vec The velocity vector
	 * @return true if the minecart is moving in a straight direction, false otherwise
	 */
	static boolean isStraight(Vector vec){
		return vec.getX()!=0 || vec.getZ()!=0 && vec.getX()*vec.getZ()==0;
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onVehicleMove(VehicleMoveEvent event) {
		int cartx, carty, cartz;
		int blockx, blocky, blockz;

		Block block;
		if (!(event.getVehicle() instanceof Minecart))
			return;

		Minecart cart = (Minecart) event.getVehicle();
		cartx = cart.getLocation().getBlockX();
		carty = cart.getLocation().getBlockY();
		cartz = cart.getLocation().getBlockZ();
		/* TODO: Slow down the Minecart automatically when it ascends or reaches a corner */
		Vector vel = cart.getVelocity();
		if (isStraight(vel)) {
			/* If the minecart is moving straight, then get the next rail it's going to touch */
			block = cart.getWorld().getBlockAt(cartx + sign(vel.getX()), carty,
					cartz + sign(vel.getY()));
		}else{
			/* Otherwise, get the rail it is on */
			block = cart.getWorld().getBlockAt(cartx,carty,cartz);
		}
		if(Tag.RAILS.isTagged(block.getType())){
			Rail rail=(Rail)block.getBlockData();
			if(rail.getShape().equals(Rail.Shape.ASCENDING_EAST)
				|| rail.getShape().equals(Rail.Shape.ASCENDING_NORTH)
				|| rail.getShape().equals(Rail.Shape.ASCENDING_SOUTH)
				|| rail.getShape().equals(Rail.Shape.ASCENDING_WEST)){
				/* TODO: Slow down the cart if it is going to ascend */
				if(cart.getPassengers().size()!=0
						&& cart.getPassengers().get(0) instanceof Player){
					cart.getPassengers().get(0).sendMessage("You are going to ascend");
				}
			}
		}
		/* Search signs and adjust speed */
		for (int xmod : xmodifier) {
			for (int ymod : ymodifier) {
				for (int zmod : zmodifier) {
					blockx = cartx + xmod;
					blocky = carty + ymod;
					blockz = cartz + zmod;
					block = cart.getWorld().getBlockAt(blockx, blocky, blockz);
					Material mat = cart.getWorld().getBlockAt(blockx, blocky, blockz).getType();

					if (isSign(mat)) {
						Sign sign = (Sign) block.getState();
						String[] text = sign.getLines();

						if (text[0].equalsIgnoreCase("[msp]")) {

							if (text[1].equalsIgnoreCase("fly")) {
								cart.setFlyingVelocityMod(flyingmod);

							} else if (text[1].equalsIgnoreCase("nofly")) {

								cart.setFlyingVelocityMod(noflyingmod);

							} else {

								error = false;
								try {

									line1 = Double.parseDouble(text[1]);

								} catch (Exception e) {

									sign.setLine(2, "  ERROR");
									sign.setLine(3, "WRONG VALUE");
									sign.update();
									error = true;

								}
								if (!error) {

									if (0 < line1 & line1 <= 50) {

										cart.setMaxSpeed(0.4D * Double.parseDouble(text[1]));

									} else {

										sign.setLine(2, "  ERROR");
										sign.setLine(3, "WRONG VALUE");
										sign.update();
									}
								}
							}
						}

					}

				}
			}
		}
	}

}
