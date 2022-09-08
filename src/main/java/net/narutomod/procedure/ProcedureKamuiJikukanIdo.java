package net.narutomod.procedure;

import net.narutomod.world.WorldKamuiDimension;
import net.narutomod.item.ItemMangekyoSharinganObito;
import net.narutomod.gui.overlay.OverlayByakuganView;
import net.narutomod.PlayerTracker;
import net.narutomod.Particles;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.Chakra;

import net.minecraft.world.World;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureKamuiJikukanIdo extends ElementsNarutomodMod.ModElement {
	public ProcedureKamuiJikukanIdo(ElementsNarutomodMod instance) {
		super(instance, 119);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("is_pressed") == null) {
			System.err.println("Failed to load dependency is_pressed for procedure KamuiJikukanIdo!");
			return;
		}
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure KamuiJikukanIdo!");
			return;
		}
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure KamuiJikukanIdo!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure KamuiJikukanIdo!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure KamuiJikukanIdo!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure KamuiJikukanIdo!");
			return;
		}
		boolean is_pressed = (boolean) dependencies.get("is_pressed");
		Entity entity = (Entity) dependencies.get("entity");
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");
		boolean f1 = false;
		boolean f2 = false;
		boolean f3 = false;
		double fov = 0;
		double distance = 0;
		double i = 0;
		double timer = 0;
		double chakraAmount = 0;
		double chakraUsage = 0;
		if ((((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY).hasTagCompound()
				&& ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY).getTagCompound()
						.getBoolean("sharingan_blinded"))) {
			if ((entity.getEntityData().getBoolean("kamui_teleport"))) {
				OverlayByakuganView.sendCustomData(entity, false, 70);
				entity.getEntityData().setBoolean("kamui_teleport", (false));
			}
			return;
		}
		timer = (double) ((entity.getEntityData().getDouble("kamui_timer")) + 1);
		chakraAmount = (double) Chakra.pathway((EntityPlayer) entity).getAmount();
		if ((!(entity.isSneaking()))) {
			if ((entity.getEntityData().getBoolean("kamui_teleport"))) {
				OverlayByakuganView.sendCustomData(entity, false, 70);
				entity.getEntityData().setBoolean("kamui_teleport", (false));
				timer = (double) 0;
			}
			chakraUsage = (double) ItemMangekyoSharinganObito.getIntangibleChakraUsage((EntityLivingBase) entity);;
			if (((chakraAmount) > (chakraUsage))) {
				ProcedureUtils.purgeHarmfulEffects((EntityLivingBase) entity);
				if (entity instanceof EntityPlayer) {
					((EntityPlayer) entity).capabilities.allowEdit = (!(is_pressed));
					((EntityPlayer) entity).sendPlayerAbilities();
				}
				ProcedureOnLivingUpdate.setNoClip(entity, is_pressed);
				if (entity instanceof EntityPlayer && !entity.world.isRemote) {
					((EntityPlayer) entity).sendStatusMessage(
							new TextComponentString(
									((net.minecraft.util.text.translation.I18n.translateToLocal("chattext.intangible")) + "" + ((is_pressed)))),
							(true));
				}
				entity.getEntityData().setBoolean("kamui_intangible", (is_pressed));
			}
			if ((!(is_pressed))) {
				timer = (double) 0;
			}
		} else {
			if ((entity.getEntityData().getBoolean("kamui_intangible"))) {
				ProcedureOnLivingUpdate.setNoClip(entity, is_pressed);
				entity.getEntityData().setBoolean("kamui_intangible", (is_pressed));
				if ((!(is_pressed))) {
					timer = (double) 0;
				}
			} else {
				RayTraceResult t = ProcedureUtils.objectEntityLookingAt(entity, 100d, true);
				x = (int) t.hitVec.x;
				y = (int) t.hitVec.y;
				z = (int) t.hitVec.z;
				f3 = (boolean) (x == (int) entity.posX && y == (int) entity.posY && z == (int) entity.posZ);
				f1 = (boolean) (t.typeOfHit != RayTraceResult.Type.MISS);
				distance = (double) entity.getDistance(x, y, z);
				if ((is_pressed)) {
					chakraUsage = (double) ItemMangekyoSharinganObito.getTeleportChakraUsage((EntityLivingBase) entity);;
					if ((((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).capabilities.isCreativeMode : false)
							|| ((f3) || ((chakraAmount) > (chakraUsage))))) {
						entity.getEntityData().setBoolean("kamui_teleport", (true));
						if ((f1)) {
							fov = (double) (70 - (Math.log((distance)) * 15));
							OverlayByakuganView.sendCustomData(entity, false, (float) fov);
						}
						if ((((timer) % 60) == 1)) {
							world.playSound((EntityPlayer) null, x, y, z, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
									.getObject(new ResourceLocation("narutomod:KamuiSFX")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
						}
						Particles.spawnParticle(world, Particles.Types.PORTAL_SPIRAL, t.hitVec.x, t.hitVec.y, t.hitVec.z, 100, 0d, 0d, 0d, 0d, 0d, 0d,
								5, 0x20000000, 30);
					} else {
						timer = (double) 0;
					}
				} else if ((entity.getEntityData().getBoolean("kamui_teleport"))) {
					entity.getEntityData().setBoolean("kamui_teleport", (false));
					OverlayByakuganView.sendCustomData(entity, false, 70);
					if ((f3)) {
						t.entityHit = entity;
					}
					int dimid = (entity.dimension != WorldKamuiDimension.DIMID) ? WorldKamuiDimension.DIMID : 0;
					f2 = (boolean) (t.entityHit != null);
					if ((f2)) {
						i = t.entityHit.getEntityBoundingBox().getAverageEdgeLength();
						i = (double) ((timer) / (((distance) * (i)) * (2.01 - (PlayerTracker.getNinjaLevel((EntityPlayer) entity) / 300.1))));
						if (((!(f3)) && ((i) <= 0.99999))) {
							if (((i) > 0)) {
								i = (double) ((i)
										* ((t.entityHit instanceof EntityLivingBase) ? ((EntityLivingBase) t.entityHit).getMaxHealth() : -1));
								if (t.entityHit instanceof EntityLivingBase)
									((EntityLivingBase) t.entityHit).setHealth(
											(float) (((t.entityHit instanceof EntityLivingBase) ? ((EntityLivingBase) t.entityHit).getHealth() : -1)
													- (i)));
							}
						} else {
							ProcedureKamuiTeleportEntity.eEntity(t.entityHit, x, z, dimid);
							if (entity instanceof EntityPlayer && !entity.world.isRemote) {
								((EntityPlayer) entity).sendStatusMessage(new TextComponentString(
										((net.minecraft.util.text.translation.I18n.translateToLocal("chattext.teleported")) + "" + (t.entityHit))),
										(false));
							}
						}
					}
					timer = (double) 0;
				}
			}
		}
		entity.getEntityData().setDouble("kamui_timer", (timer));
	}
}