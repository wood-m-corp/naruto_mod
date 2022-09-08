
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.DungeonHooks;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.BossInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemZabuzaSword;
import net.narutomod.item.ItemSuiton;
import net.narutomod.ElementsNarutomodMod;

import javax.annotation.Nullable;
import com.google.common.base.Predicate;

@ElementsNarutomodMod.ModElement.Tag
public class EntityZabuzaMomochi extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 154;
	public static final int ENTITYID_RANGED = 155;

	public EntityZabuzaMomochi(ElementsNarutomodMod instance) {
		super(instance, 411);
	}

	@Override
	public void initElements() {
		elements.entities
		  .id(new ResourceLocation("narutomod", "zabuza_momochi"), ENTITYID)
		  .tracker(64, 3, true).egg(-6710887, -16764058).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		Biome[] spawnBiomes = {Biome.REGISTRY.getObject(new ResourceLocation("plains")),
				Biome.REGISTRY.getObject(new ResourceLocation("extreme_hills")), Biome.REGISTRY.getObject(new ResourceLocation("forest")),
				Biome.REGISTRY.getObject(new ResourceLocation("taiga")), Biome.REGISTRY.getObject(new ResourceLocation("swampland")),
				Biome.REGISTRY.getObject(new ResourceLocation("river")), Biome.REGISTRY.getObject(new ResourceLocation("jungle")),
				Biome.REGISTRY.getObject(new ResourceLocation("savanna")), Biome.REGISTRY.getObject(new ResourceLocation("ice_mountains")),
				Biome.REGISTRY.getObject(new ResourceLocation("ice_flats")), Biome.REGISTRY.getObject(new ResourceLocation("beaches")),
				Biome.REGISTRY.getObject(new ResourceLocation("cold_beach")),};
		EntityRegistry.addSpawn(EntityCustom.class, 1, 1, 1, EnumCreatureType.MONSTER, spawnBiomes);
		DungeonHooks.addDungeonMob(new ResourceLocation("narutomod:zabuza_momochi"), 180);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> new RenderCustom(renderManager));
	}

	public static class EntityCustom extends EntityNinjaMob.Base implements IRangedAttackMob, IMob {
		private final int MIST_CD = 1200;
		private final int WATERPRISON_CD = 400;
		private final int WATERDRAGON_CD = 300;
		private final int WATERCLONE_CD = 100;
		private final int BLOCKING_CD = 20;
		private final double MIST_CHAKRA = 100d;
		private final double WATERPRISON_CHAKRA = 200d;
		private final double WATERDRAGON_CHAKRA = 200d;
		private final double WATERCLONE_CHAKRA = 500d;
		private int mistLastUsed = -MIST_CD;
		private int prisonLastUsed = -WATERPRISON_CD + 40;
		private int cloneLastUsed = -WATERCLONE_CD + 40;
		private int lastBlockTime;
		private int lastCallForHelp;
		private EntityCustom original;
		private int clones;
		private EntityHaku.EntityCustom haku;
		private EntityLivingBase avoidTarget;
		private final EntityAINearestAttackableTarget aiTargetPlayer = new EntityAINearestAttackableTarget(this, EntityPlayer.class, true, false);
		private final EntityAIHurtByTarget aiTargetHurt = new EntityAIHurtByTarget(this, true);

		public EntityCustom(World world) {
			super(world, 80, 5000d);
			this.setSize(0.6f, 2.0f);
			this.setAttackTargetsTasks();
			//this.setItemToInventory(swordStack);
			//this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, swordStack);
			//this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ItemSuiton.block, (int) (1)));
			java.util.Arrays.fill(this.inventoryHandsDropChances, 0.0F);
		}

		public EntityCustom(EntityCustom cloneFrom) {
			this(cloneFrom.world);
			this.original = cloneFrom;
			++this.original.clones;
			this.copyLocationAndAnglesFrom(cloneFrom);
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1D);
			this.setHealth(10f);
			this.targetTasks.addTask(3, new EntityNinjaMob.AIDefendEntity(this, this.original));
			this.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(this)), null);
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			livingdata = super.onInitialSpawn(difficulty, livingdata);
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ItemZabuzaSword.block));
			this.spawnHaku();
			return livingdata;
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityLivingBase.class, new Predicate<Entity>() {
				public boolean apply(@Nullable Entity p_apply_1_) {
					return p_apply_1_ != null && p_apply_1_.equals(EntityCustom.this.avoidTarget);
				}
			}, 10f, 1.25d, 1.25d) {
				@Override
				public boolean shouldExecute() {
					if (EntityCustom.this.getHealth() > EntityCustom.this.getMaxHealth() * 0.6f 
					 && EntityCustom.this.avoidTarget != null) {
						EntityCustom.this.avoidTarget = null;
						EntityCustom.this.setAttackTargetsTasks();
						return false;
					}
					return super.shouldExecute();
				}
			});
			this.tasks.addTask(2, new EntityNinjaMob.AIAttackRangedJutsu(this, WATERDRAGON_CD, 12.0F));
			this.tasks.addTask(2, new EntityNinjaMob.AILeapAtTarget(this, 1.0f));
			this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.5d, true) {
				@Override
				protected double getAttackReachSqr(EntityLivingBase attackTarget) {
					return 5.3d + attackTarget.width;
				}
			});
			this.tasks.addTask(4, new EntityAIWander(this, 0.5));
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10D);
		}

		@Override
		protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
			if (!this.isClone()) {
				this.entityDropItem(this.getHeldItemMainhand(), 0f);
				this.entityDropItem(this.getItemFromInventory(0), 0f);
			}
		}

		public boolean isClone() {
			return this.original != null;
		}

		@Override
		public boolean isOnSameTeam(Entity entityIn) {
			return super.isOnSameTeam(entityIn) || EntityNinjaMob.TeamZabuza.contains(entityIn.getClass());
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (!this.isAIDisabled() && !this.getHeldItemMainhand().isEmpty() && !this.isActiveItemStackBlocking()) {
				this.setActiveHand(EnumHand.MAIN_HAND);
				this.activeItemStackUseCount = this.getActiveItemStack().getMaxItemUseDuration() - 5;
				this.lastBlockTime = this.ticksExisted;
			}
			return super.attackEntityFrom(source, amount);
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();

			EntityLivingBase target = this.getAttackTarget();
			//if (this.getHeldItemMainhand().isEmpty() != (target == null && this.avoidTarget == null)) {
			//	this.swapWithInventory(EntityEquipmentSlot.MAINHAND);
			//}
			if (target != null && target.isEntityAlive()) {
				double distanceToTarget = this.getDistance(target);
				if (!this.isClone()
				 && this.ticksExisted > this.mistLastUsed + MIST_CD && this.consumeChakra(MIST_CHAKRA)) {
					new ItemSuiton.EntityMist.Jutsu().createJutsu(this.getHeldItemOffhand(), this, 1f);
					this.mistLastUsed = this.ticksExisted;
				}
				if (this.isClone() && !EntityWaterPrison.isEntityTrapped(target) && distanceToTarget >= 2d
				 && this.ticksExisted > this.prisonLastUsed + WATERPRISON_CD && this.getChakra() >= WATERPRISON_CHAKRA) {
			 		this.getLookHelper().setLookPositionWithEntity(target, 90f, 30f);
				 	if (new EntityWaterPrison.EC.Jutsu().createJutsu(this, target, 300) != null) {
				 		this.swapWithInventory(EntityEquipmentSlot.MAINHAND, 0);
						this.consumeChakra(WATERPRISON_CHAKRA);
						this.prisonLastUsed = this.ticksExisted;
					}
				}
				if (!this.isClone() && this.clones < 1
				 && this.ticksExisted > this.cloneLastUsed + WATERCLONE_CD && this.consumeChakra(WATERCLONE_CHAKRA)) {
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:kagebunshin")), 1f, 1f);
					this.world.spawnEntity(new EntityCustom(this));
					this.cloneLastUsed = this.ticksExisted;
				}
			//} else {
			//	this.setAttackTarget(null);
			}
			if (!this.isClone() && this.getHealth() <= this.getMaxHealth() * 0.4f && this.getRevengeTarget() != null
			 && this.ticksExisted > this.lastCallForHelp + 40) {
			    this.avoidTarget = this.getRevengeTarget();
			    this.removeTargetsTasks();
			    this.callHelp();
			    this.lastCallForHelp = this.ticksExisted;
			}
		}

		private void callHelp() {
		    double d0 = ProcedureUtils.getFollowRange(this);
            for (Class<? extends EntityNinjaMob.Base> oclass : EntityNinjaMob.TeamZabuza) {
			    for (EntityNinjaMob.Base ninja : 
			     this.world.getEntitiesWithinAABB(oclass, this.getEntityBoundingBox().grow(d0, 8.0D, d0))) {
			        if (ninja != this && !ninja.isOnSameTeam(this.avoidTarget)) {
			        	ninja.setAttackTarget(this.avoidTarget);
			        }
                }
		    }
		}

		public void removeTargetsTasks() {
			this.targetTasks.removeTask(this.aiTargetPlayer);
			this.targetTasks.removeTask(this.aiTargetHurt);
			this.setAttackTarget(null);
		}

		public void setAttackTargetsTasks() {
			this.targetTasks.addTask(1, this.aiTargetPlayer);
			this.targetTasks.addTask(2, this.aiTargetHurt);
		}

		@Override
		protected void onDeathUpdate() {
			if (!this.world.isRemote && this.isClone()) {
				this.playSound(net.minecraft.init.SoundEvents.ENTITY_GENERIC_SPLASH, 1f, 1f);
				new net.narutomod.event.EventSetBlocks(this.world,
				 com.google.common.collect.ImmutableMap.of(new BlockPos(this).up(), 
				 Blocks.FLOWING_WATER.getDefaultState()), 0, 10, false, false);
				this.setDead();
			} else {
				super.onDeathUpdate();
			}
		}

		@Override
		public void setDead() {
			super.setDead();
			if (!this.world.isRemote && this.isClone()) {
				--this.original.clones;
			}
		}

		@Override
		public void setSwingingArms(boolean swingingArms) {
		}

		@Override
		public void attackEntityWithRangedAttack(EntityLivingBase target, float flval) {
			if (!this.world.isRemote && this.getChakra() >= WATERDRAGON_CHAKRA) {
				new EntityWaterDragon.EC.Jutsu().createJutsu(this, 1f);
				this.consumeChakra(WATERDRAGON_CHAKRA);
				this.standStillFor(60);
			}
		}

		@Override
		public boolean getCanSpawnHere() {
			return super.getCanSpawnHere() 
			 && this.world.getEntitiesWithinAABB(EntityCustom.class, this.getEntityBoundingBox().grow(128.0D)).isEmpty()
			 && this.rand.nextInt(5) == 0;
		}

		@Override
		public boolean isNonBoss() {
			return this.isClone();
		}

		private final BossInfoServer bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);

		@Override
		public void addTrackingPlayer(EntityPlayerMP player) {
			super.addTrackingPlayer(player);
			if (!this.isClone() && (player.equals(this.getAttackingEntity()) || player.equals(this.getAttackTarget()))) {
				this.bossInfo.addPlayer(player);
			}
		}

		@Override
		public void removeTrackingPlayer(EntityPlayerMP player) {
			super.removeTrackingPlayer(player);
			if (!this.isClone()) {
				this.bossInfo.removePlayer(player);
			}
		}

		private void trackAttackedPlayers() {
			Entity entity = this.getAttackingEntity();
			if (entity instanceof EntityPlayerMP || (entity = this.getAttackTarget()) instanceof EntityPlayerMP) {
				this.bossInfo.addPlayer((EntityPlayerMP)entity);
			} else {
				java.util.List<EntityPlayerMP> list = new java.util.ArrayList<EntityPlayerMP>();
				for (EntityPlayerMP entityplayermp : this.bossInfo.getPlayers())
					list.add(entityplayermp);
				for (EntityPlayerMP entityplayermp : list)
					this.bossInfo.removePlayer(entityplayermp);
			}
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.isActiveItemStackBlocking() && this.ticksExisted > this.lastBlockTime + this.BLOCKING_CD) {
				this.resetActiveHand();
			}
			if (!this.isClone()) {
				this.trackAttackedPlayers();
				this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
				if (!this.world.isRemote && this.ticksExisted > 20 && this.haku == null) {
					this.spawnHaku();
				}
			} else if (!this.world.isRemote) {
				this.setNoAI(EntityWaterPrison.isEntityTrapping(this));
				if (!this.original.isEntityAlive() || (this.original.getAttackTarget() == null && this.original.avoidTarget == null)) {
					this.setHealth(0f);
				}
			}
		}

		private void spawnHaku() {
			if (!this.isClone()) {
				this.haku = (EntityHaku.EntityCustom)this.world
				 .findNearestEntityWithinAABB(EntityHaku.EntityCustom.class, this.getEntityBoundingBox().grow(128d, 32d, 128d), this);
				if (this.haku == null) {
					this.haku = new EntityHaku.EntityCustom(this.world);
					this.haku.setLeader(this);
					this.haku.setPosition(this.posX + (this.rand.nextBoolean()?3d:-3d), this.posY, this.posZ + (this.rand.nextBoolean()?3d:-3d));
					this.world.spawnEntity(this.haku);
				} else {
					this.haku.setLeader(this);
				}
			}
		}

		@Override
		public void writeEntityToNBT(NBTTagCompound compound) {
			super.writeEntityToNBT(compound);
			if (this.isClone()) {
				compound.setUniqueId("originalUUID", this.original.getUniqueID());
			}
		}

		@Override
		public void readEntityFromNBT(NBTTagCompound compound) {
			super.readEntityFromNBT(compound);
			if (this.world instanceof WorldServer && compound.hasUniqueId("originalUUID")) {
				Entity entity = ((WorldServer)this.world).getEntityFromUuid(compound.getUniqueId("originalUUID"));
				if (entity instanceof EntityCustom) {
					this.original = (EntityCustom)entity;
				} else {
					this.setDead();
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public class RenderCustom extends EntityNinjaMob.RenderBase<EntityCustom> {
		private final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/zabuzamomochi.png");

		public RenderCustom(RenderManager renderManager) {
			//super(renderManager, new ModelBiped64(), 0.5f);
			//this.addLayer(new EntityNinjaMob.LayerInventoryItem(this));
			super(renderManager, new ModelBiped64());
		}

		/*@Override
		public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
			((ModelBiped)this.mainModel).rightArmPose = ModelBiped.ArmPose.EMPTY;
			((ModelBiped)this.mainModel).leftArmPose = ModelBiped.ArmPose.EMPTY;
			if (!entity.getHeldItemMainhand().isEmpty()) {
				ModelBiped.ArmPose armpos = entity.getItemInUseCount() > 0 ? ModelBiped.ArmPose.BLOCK : ModelBiped.ArmPose.ITEM;
				if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
					((ModelBiped)this.mainModel).rightArmPose = armpos;
				} else {
					((ModelBiped)this.mainModel).leftArmPose = armpos;
				}
			}
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}*/

		@Override
		protected ResourceLocation getEntityTexture(EntityCustom entity) {
			return TEXTURE;
		}
	}

	@SideOnly(Side.CLIENT)
	public class ModelBiped64 extends ModelBiped {
		public ModelBiped64() {
			this.textureWidth = 64;
			this.textureHeight = 64;
			this.leftArmPose = ModelBiped.ArmPose.EMPTY;
			this.rightArmPose = ModelBiped.ArmPose.EMPTY;
			this.bipedHead = new ModelRenderer(this);
			this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
			this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
			this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 24, 0, -2.0F, -10.0F, 3.0F, 4, 4, 4, 0.0F, false));
			this.bipedHeadwear = new ModelRenderer(this);
			this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
			this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F, false));
			this.bipedBody = new ModelRenderer(this);
			this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
			this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
			this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F, false));
			this.bipedRightArm = new ModelRenderer(this);
			this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
			this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
			this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, false));
			this.bipedLeftArm = new ModelRenderer(this);
			this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
			this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 32, 48, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
			this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 48, 48, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, false));
			this.bipedRightLeg = new ModelRenderer(this);
			this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
			this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
			this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
			this.bipedLeftLeg = new ModelRenderer(this);
			this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
			this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 16, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
			this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 0, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
		}
	}
}