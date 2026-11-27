precision mediump float;
uniform sampler2D sampler0;
uniform vec2 u_picture; // Taho: brightness, contrast factors; the app sets them (1, 1 = unchanged)
varying vec2 v_texcoord0;
void main() {
    vec4 c = texture2D(sampler0, v_texcoord0);
    gl_FragColor = vec4(clamp((c.rgb * u_picture.x - 0.5) * u_picture.y + 0.5, 0.0, 1.0), c.a);
}