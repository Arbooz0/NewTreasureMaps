class_name AccordionGenerator
extends Generator




func get_name() -> String:
	return "accordion"


func _get_scale() -> float:
	return 0.18




func _anim_pos(pos: Vector2i, uv: Vector2, init_pos: Vector3) -> Vector3:
	var to: Vector3 = init_pos
	to.x /= lerpf(40, 8, interpolation(uv.y))
	to.z = 0.5 if (pos.x % 2 == 0) else -0.5
	return init_pos.lerp(to, interpolation_time(uv.x))


func interpolation_time(x: float) -> float:
	x = 0.5 - absf(x - 0.5)
	var t: float = time + x
	if t > 1:
		t = 1
	return lerpf(time, t, time)



func interpolation(v: float) -> float:
	v = absf(v - 0.5) * 2
	v = 2 - v
	v = v ** 3
	v -= 1
	v /= 7.0
	v = 1 - v
	return v
