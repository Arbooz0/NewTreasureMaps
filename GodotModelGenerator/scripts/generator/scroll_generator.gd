class_name ScrollGenerator
extends Generator




func get_name() -> String:
	return "scroll"


func _get_size() -> Vector2:
	return Vector2(11, 23)


func _get_scale() -> float:
	return 0.16




func _anim_pos(pos: Vector2i, uv: Vector2, init_pos: Vector3) -> Vector3:
	var t: float = lerpf(0.53, 1, time)
	var y: float = lerpf(init_grid.grid[0].y, init_grid.grid[-1].y, t)
	var add_y: float = y * -1 + ((1 - t) * init_grid.grid[0].y)
	
	if uv.y > t:
		init_pos.y += add_y
		return init_pos
	
	var l: float = t - uv.y
	var r_max: float = 0.3 + (t / 2)
	var r: float = r_max - (l / 2)
	var v: Vector2 = Vector2.from_angle(l * 20.0)
	v *= r
	v.x *= -1
	init_pos.y = y + v.y
	init_pos.z += v.x + r_max
	
	init_pos.y += add_y
	
	return init_pos
