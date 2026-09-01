#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:fog.glsl>

// Supplied by EchoesSkyPass. A compositor pass has no usable modelview matrix, so the view half of
// the transform comes in by hand; without it the effect is screen-locked.
layout(std140) uniform EchoesSky {
    mat4 CameraToWorld;   // rotation only
    vec4 CameraWorld;     // xyz camera position, w seconds
};

uniform sampler2D InputColor;
// Declared by the pipeline but unused; Minecraft logs a benign "does not use sampler" warning.
uniform sampler2D SceneColor;
uniform sampler2D SceneDepth;

in vec2 texCoord;

out vec4 fragColor;

// Cost knob: each octave is three simplex evaluations per sky pixel.
const int OCTAVES = 4;

const float TIME_SCALE = 1.0;

// Fixed world bearing, not the zenith - the zenith is overhead everywhere, so it would follow you.
const vec3 VORTEX_DIR = vec3(0.35220, 0.55346, -0.75472);  // normalize(0.35, 0.55, -0.75)
const float VORTEX_SCALE = 1.6;
// Radii, in the vortex's own uv, where the disc fades into deep space.
const float DISC_INNER = 0.9;
const float DISC_OUTER = 1.9;
// The dark eye at the middle.
const float CORE_RADIUS = 0.20;
// Photon ring on the horizon. Driven by radius, not the noise, so the line stays crisp.
const vec3 RIM_COLOR = vec3(1.0);
const float RIM_STRENGTH = 0.40;
// Half-width, as a fraction of CORE_RADIUS.
const float RIM_WIDTH = 0.13;

// Outline traced along a contour of the structure term, so it follows the arms' shape.
const float SWIRL_EDGE = 0.5;
const float SWIRL_EDGE_WIDTH = 0.07;
const float SWIRL_RIM_STRENGTH = 1.3;

// Flat bands the disc is quantised into. Fewer reads as more drawn.
const float CEL_LEVELS = 4.0;

// Two shades rather than one tint, so the disc shifts hue with brightness instead of just getting
// paler - dim areas sit deep blue, hot areas run to pale sky.
const vec3 HALO_BRIGHT = vec3(0.62, 0.85, 1.00);
const vec3 HALO_DEEP = vec3(0.16, 0.38, 0.62);
const vec3 SPACE_COLOR = vec3(0.008, 0.010, 0.022);
const float STAR_DENSITY = 220.0;

// Mist band. Tile floors sit at y=4, walls top out near y=21, and the dimension is 32 tall.
// The base deliberately overlaps the top of the walls so blocks and the sky beside them pick up
// comparable haze; without the overlap, lit stone meets near-black sky with nothing between.
const float FOG_BASE_Y = 14.0;
const float FOG_TOP_Y = 31.0;
// Optical density per block inside the band.
const float FOG_DENSITY = 0.04;
const float FOG_STRENGTH = 0.9;
const float FOG_SKY_DISTANCE = 300.0;
// Thin fog with no height limit. The band alone is only entered by rays that climb into it, so
// horizontal views along a corridor would otherwise pick up no fog at all.
const float HAZE_DENSITY = 0.004;

vec4 permute_3d(vec4 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
vec4 taylorInvSqrt3d(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float simplexNoise3d(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1.0 + 3.0 * C.xxx;

    i = mod(i, 289.0);
    vec4 p = permute_3d(permute_3d(permute_3d(
        i.z + vec4(0.0, i1.z, i2.z, 1.0)) +
        i.y + vec4(0.0, i1.y, i2.y, 1.0)) +
        i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 1.0 / 7.0;
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt3d(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}

// Constant bound so the compiler can unroll.
float fbm3d(vec3 x) {
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(100.0);

    for (int i = 0; i < OCTAVES; ++i) {
        v += a * simplexNoise3d(x);
        x = x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

vec3 rotateZ(vec3 v, float angle) {
    float cosAngle = cos(angle);
    float sinAngle = sin(angle);
    return vec3(
        v.x * cosAngle - v.y * sinAngle,
        v.x * sinAngle + v.y * cosAngle,
        v.z
    );
}

float facture(vec3 vector) {
    vec3 normalizedVector = normalize(vector);
    return max(max(normalizedVector.x, normalizedVector.y), normalizedVector.z);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

// Keyed to world direction, so stars hold still while you turn.
vec3 starField(vec3 direction) {
    vec3 p = direction * STAR_DENSITY;
    vec3 cell = floor(p);

    float present = step(0.9915, hash13(cell));
    vec3 jitter = vec3(hash13(cell + 11.0), hash13(cell + 23.0), hash13(cell + 37.0)) - 0.5;
    float spark = smoothstep(0.55, 0.0, length(p - (cell + 0.5 + jitter * 0.7)));

    vec3 tint = mix(vec3(0.75, 0.82, 1.0), vec3(1.0, 0.88, 0.72), hash13(cell + 53.0));
    return present * spark * tint;
}

vec3 deepSpace(vec3 direction) {
    return SPACE_COLOR + starField(direction);
}

vec3 skyColor(vec3 direction) {
    // Gnomonic projection about VORTEX_DIR: uv is (0,0) at the hole and grows outwards.
    vec3 forward = VORTEX_DIR;
    vec3 right = normalize(cross(vec3(0.0, 1.0, 0.0), forward));
    vec3 up = cross(forward, right);

    float facing = dot(direction, forward);
    vec2 uv = vec2(dot(direction, right), dot(direction, up)) / max(facing, 0.05) * VORTEX_SCALE;

    float radius = length(uv);
    // step() kills the mirrored ghost a gnomonic projection puts behind you.
    float disc = (1.0 - smoothstep(DISC_INNER, DISC_OUTER, radius)) * step(0.05, facing);

    // Skip the noise outside the disc. Coherent across large screen regions, so divergence is cheap.
    if (disc <= 0.001) {
        return deepSpace(direction);
    }

    float time = CameraWorld.w * TIME_SCALE;

    vec3 color = normalize(vec3(uv, 0.5));
    color -= 0.2 * vec3(0.0, 0.0, time);

    float angle = -log2(max(radius, 1e-4));
    color = rotateZ(color, angle);

    float frequency = 1.4;
    float distortion = 0.01;
    color.x = fbm3d(color * frequency + 0.0) + distortion;
    color.y = fbm3d(color * frequency + 1.0) + distortion;
    color.z = fbm3d(color * frequency + 2.0) + distortion;

    vec3 noiseColor = color;
    noiseColor *= 2.0;
    noiseColor -= 0.1;
    noiseColor *= 0.188;
    noiseColor += vec3(uv, 0.0);

    float glow = (0.770 - length(noiseColor)) * 4.2;

    // Clamped: the original mixes with a weight outside [0,1], which runs away to white once uv is
    // no longer bounded by the screen.
    float fac = (radius - facture(color + 0.32) + 0.1) * 3.0;
    vec3 filament = HALO_DEEP * 0.35 + vec3(0.02, 0.03, 0.06);
    // Clamped at the top, not just floored at zero: unbounded, glow runs past white near the middle
    // and every inner pixel lands in the same cel band, flattening the centre.
    float intensity = clamp(glow * 0.4, 0.0, 1.0);
    vec3 halo = mix(HALO_DEEP, HALO_BRIGHT, intensity) * intensity;
    vec3 vortex = mix(halo, filament, clamp(fac, 0.0, 1.0));

    // Cel step. Dividing the original luminance back out banks intensity only, so the banding does
    // not shift hue.
    float lum = max(max(vortex.r, max(vortex.g, vortex.b)), 1e-4);
    vortex *= (floor(lum * CEL_LEVELS + 0.5) / CEL_LEVELS) / lum;

    // Event horizon, then the ring on its edge. Tight ramp: a wide one reads as a grey halo.
    vortex *= smoothstep(CORE_RADIUS * 0.94, CORE_RADIUS * 1.06, radius);
    float rim = 1.0 - smoothstep(0.0, CORE_RADIUS * RIM_WIDTH, abs(radius - CORE_RADIUS));
    vortex += RIM_COLOR * rim * RIM_STRENGTH;

    // Outline the arms. Added after the core punch, which would otherwise scrub it away.
    float outward = smoothstep(CORE_RADIUS * 2.0, DISC_INNER * 0.8, radius);
    float edge = 1.0 - smoothstep(0.0, SWIRL_EDGE_WIDTH, abs(fac - SWIRL_EDGE));
    vortex += RIM_COLOR * edge * SWIRL_RIM_STRENGTH * outward;

    return mix(deepSpace(direction), vortex, disc);
}

// Length of ray inside a horizontal slab. Analytic, so fog costs the same at any thickness.
vec2 slabRange(float originY, float dirY, float low, float high, float travel) {
    if (abs(dirY) < 1e-5) {
        return (originY > low && originY < high) ? vec2(0.0, travel) : vec2(0.0);
    }
    float ta = (low - originY) / dirY;
    float tb = (high - originY) / dirY;
    return vec2(clamp(min(ta, tb), 0.0, travel), clamp(max(ta, tb), 0.0, travel));
}

void main() {
    vec2 ndc = texCoord * 2.0 - 1.0;

    // Camera-space ray from ProjMat, rotated into world space by the matrix the pass uploads.
    mat4 inverseProjection = inverse(ProjMat);
    vec4 farPoint = inverseProjection * vec4(ndc, 1.0, 1.0);
    vec3 rayCamera = farPoint.xyz / farPoint.w;
    vec3 direction = normalize(mat3(CameraToWorld) * rayCamera);

    float sceneDepth = texture(SceneDepth, texCoord).r;
    bool isSky = sceneDepth >= 0.999999;

    vec3 color;
    float travel;
    if (isSky) {
        color = skyColor(direction);
        travel = FOG_SKY_DISTANCE;
    } else {
        // Where the world drew something, keep it and only lay fog over it.
        color = texture(InputColor, texCoord).rgb;

        // Camera space is metric, so the unprojected length is the distance travelled.
        vec4 scenePoint = inverseProjection * vec4(ndc, sceneDepth * 2.0 - 1.0, 1.0);
        travel = length(scenePoint.xyz / scenePoint.w);
    }

    // Both fogs share one exponential - summing optical depths keeps them a single medium.
    vec2 band = slabRange(CameraWorld.y, direction.y, FOG_BASE_Y, FOG_TOP_Y, travel);
    float through = max(band.y - band.x, 0.0);

    // The haze is the dimension's air, so it stops at the ceiling rather than running the full sky
    // distance, which would charge an upward ray for atmosphere that is not there.
    vec2 air = slabRange(CameraWorld.y, direction.y, -1000.0, FOG_TOP_Y, travel);
    float hazeThrough = max(air.y - air.x, 0.0);

    float optical = through * FOG_DENSITY + hazeThrough * HAZE_DENSITY;
    float fog = (1.0 - exp(-optical)) * FOG_STRENGTH;

    fragColor = vec4(mix(color, FogColor.rgb, fog), 1.0);
}