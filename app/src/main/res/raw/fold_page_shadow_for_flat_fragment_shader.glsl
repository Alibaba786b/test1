precision mediump float;

varying float vAlphaRatio;

void main() {
    gl_FragColor = vec4(vec3(0.5), 0.8 * vAlphaRatio);
}
