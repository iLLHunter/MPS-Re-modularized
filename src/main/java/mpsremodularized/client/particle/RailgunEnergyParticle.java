package mpsremodularized.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/** Full-bright energy with a hot core, lime body and emerald edge. */
public final class RailgunEnergyParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean flash;
    private final boolean warmHighlight;

    private RailgunEnergyParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, boolean flash) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.flash = flash;
        this.warmHighlight = flash || random.nextFloat() < 0.2F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = flash ? 3 : 6 + random.nextInt(3);
        this.quadSize = flash ? 0.32F : 0.06F + random.nextFloat() * 0.04F;
        this.friction = 0.82F;
        this.gravity = 0;
        this.hasPhysics = false;
        this.roll = this.oRoll = random.nextFloat() * (float) (Math.PI * 2);
        setColor(0.5F, 1.0F, 0.05F);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        this.alpha = Math.max(0, 1.0F - (float) age / lifetime);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return quadSize * Math.max(0, 1.0F - 0.5F * (age + partialTick) / lifetime);
    }

    @Override
    public void render(VertexConsumer vertices, Camera camera, float partialTick) {
        // Sodium short-circuits both camera-based SingleQuadParticle entry points.
        // Apply their normal transform here, then enter our coordinate-based layers directly.
        Quaternionf rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTick);
        if (roll != 0.0F) rotation.rotateZ(Mth.lerp(partialTick, oRoll, roll));
        var position = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, xo, this.x) - position.x());
        float y = (float) (Mth.lerp(partialTick, yo, this.y) - position.y());
        float z = (float) (Mth.lerp(partialTick, zo, this.z) - position.z());
        renderRotatedQuad(vertices, rotation, x, y, z, partialTick);
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer vertices, Quaternionf rotation,
                                     float x, float y, float z, float partialTick) {
        float originalSize = quadSize;
        float originalAlpha = alpha;
        float originalRed = rCol;
        float originalGreen = gCol;
        float originalBlue = bCol;
        float progress = Mth.clamp((age + partialTick) / lifetime, 0.0F, 1.0F);

        try {
            // Keep the shaded edge inside the original particle's footprint.
            setColor(0.08F, 0.5F, 0.18F);
            alpha = originalAlpha * 0.65F;
            super.renderRotatedQuad(vertices, rotation, x, y, z, partialTick);

            // Lime remains dominant, cooling toward emerald as the particle fades.
            quadSize = originalSize * 0.82F;
            alpha = originalAlpha;
            setColor(Mth.lerp(progress, 0.5F, 0.12F),
                    Mth.lerp(progress, 1.0F, 0.65F), Mth.lerp(progress, 0.05F, 0.2F));
            // Move inner layers very slightly toward the camera to avoid coplanar depth flicker.
            super.renderRotatedQuad(vertices, rotation, x * 0.99998F, y * 0.99998F, z * 0.99998F, partialTick);

            quadSize = originalSize * (flash ? 0.34F : 0.28F);
            alpha = originalAlpha * (1.0F - progress) * 0.95F;
            if (warmHighlight) {
                setColor(1.0F, flash ? 1.0F : 0.96F, flash ? Mth.lerp(progress, 0.92F, 0.5F) : 0.55F);
            } else {
                setColor(0.75F, 1.0F, 0.35F);
            }
            super.renderRotatedQuad(vertices, rotation, x * 0.99996F, y * 0.99996F, z * 0.99996F, partialTick);
        } finally {
            quadSize = originalSize;
            alpha = originalAlpha;
            setColor(originalRed, originalGreen, originalBlue);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites, boolean flash) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new RailgunEnergyParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, flash);
        }
    }
}
