package fi.dy.esav.Minecart_speedplus;

import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
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

	private int[] xmodifier = {-1, 0, 1};
	private int[] ymodifier = {-2, -1, 0, 1, 2};
	private int[] zmodifier = {-1, 0, 1};

	private Minecart_speedplus plugin;

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
	private static int sign(double x){
		if(x>0.0)
			return 1;
		if(x<0.0)
			return -1;
		return 0;
	}

	/**
	 * Send messages to passengers of a vehicle
	 * @param vehicle Vehicle object
	 * @param message Message content
	 */
	private void notifyPassenger(Vehicle vehicle,String message){
		for(Entity passenger:vehicle.getPassengers()) {
			if(plugin.getMetadata(passenger,"debug")!=null
					&& (boolean)plugin.getMetadata(passenger,"debug"))
				passenger.sendMessage(message);
		}
	}

	/**
	 * Toggle the velocity status of the minecart
	 * @param cart The minecart object
	 * @param flag true: enables slow mode, false: disables slow mode
	 */
	private void slow(Minecart cart,boolean flag){
		double ms=0.0;
		if(flag && plugin.getMetadata(cart,"slow")==null){
			/* If we were to make the minecart slow,
			 * then save its current speed information first */
			plugin.setMetadata(cart,"slow",true);
			ms=cart.getMaxSpeed();
			plugin.setMetadata(cart,"maxSpeed",ms);
			notifyPassenger(cart,"Slow mode on (old maxSpeed="+ms+")");
			cart.setMaxSpeed(0.4);
		}else if(!flag
				&& plugin.getMetadata(cart,"slow")!=null
				&& (boolean)plugin.getMetadata(cart,"slow")) {
			ms=(double)plugin.getMetadata(cart, "maxSpeed");
			cart.setMaxSpeed(ms);
			plugin.setMetadata(cart,"slow",false);
			notifyPassenger(cart,"Slow mode off (restoring maxSpeed="+ms+")");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVehicleMove(VehicleMoveEvent event) {
		boolean error;
		int cartx, carty, cartz;
		int blockx, blocky, blockz;
		double line1=-1;

		Block block;
		if (!(event.getVehicle() instanceof Minecart))
			return;

		Minecart cart = (Minecart) event.getVehicle();

		/* Store its current velocity parameters so that we can recover it when it is not in slow mode */
		cartx = cart.getLocation().getBlockX();
		carty = cart.getLocation().getBlockY();
		cartz = cart.getLocation().getBlockZ();
		Vector vel = cart.getVelocity();
		/* Slow down the Minecart automatically when it ascends or reaches a corner */
		if (vel.getX()*vel.getZ()==0 && vel.getX()!=vel.getZ()) {
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
				|| rail.getShape().equals(Rail.Shape.ASCENDING_WEST)
				|| rail.getShape().equals(Rail.Shape.NORTH_EAST)
				|| rail.getShape().equals(Rail.Shape.NORTH_WEST)
				|| rail.getShape().equals(Rail.Shape.SOUTH_EAST)
				|| rail.getShape().equals(Rail.Shape.SOUTH_WEST)){
				slow(cart,true);
			}else{
				slow(cart,false);
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

									if (0 < line1 && line1 <= 50) {

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
