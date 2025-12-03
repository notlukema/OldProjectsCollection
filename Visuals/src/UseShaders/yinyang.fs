
#version 330 core

in vec2 f_pos;
in vec2 f_uv;

uniform float dir;

out vec4 frag_color;

void main() {
    //float alpha = 1.0;
    //float blend = 0.005;
    //float dis = f_pos.x*f_pos.x + f_pos.y*f_pos.y;
    //if (dis > 1) {
    //    if (dis > (1+blend)*(1+blend)) {
    //        return;
    //    } else {
    //        alpha = 1.0 - (sqrt(dis)-1)/blend;
    //    }
    //}
    if (f_pos.x*f_pos.x + f_pos.y*f_pos.y > 1.0) {
        frag_color = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }

    float dy = abs(0.5 - abs(f_pos.y));
    float compare = 0.25 - dy*dy;

    if (f_pos.y < 0.0) {
        compare = -compare;
    }

    float color = (f_pos.x*abs(f_pos.x) < compare) ? 1.0 : 0.0;
    if (f_pos.x*f_pos.x + dy*dy < 0.01) {
        color = 1.0 - color;
    }

	frag_color = vec4(color, color, color, 1.0);
}
