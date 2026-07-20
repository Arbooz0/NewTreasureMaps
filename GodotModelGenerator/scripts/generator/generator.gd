class_name Generator
extends RefCounted



var time: float

var init_grid: Grid = Grid.new(_get_size())
var grid: Grid



func _init() -> void:
	_init_grid()



func _get_size() -> Vector2:
	return Vector2(11, 11)



func get_name() -> String:
	return "base"



func _init_grid():
	var size: Vector2 = init_grid.size
	var start: Vector2 = Vector2(size.x / -2 + 0.5, size.y / 2 - 0.5)
	for y in size.x:
		for x in size.y:
			var pos: Vector3 = Vector3(start.x + x, start.y - y, 0)
			init_grid.set_pos(x, y, _init_grid_pos(Vector2i(x, y), pos))
	
	grid = init_grid.duplicate()



func _init_grid_pos(pos: Vector2i, value: Vector3) -> Vector3:
	value.z = randf() / 5.0
	value.x += randf_range(-0.1, 0.1)
	value.y += randf_range(-0.1, 0.1)
	return value



func update():
	var size: Vector2 = init_grid.size
	var start: Vector2 = Vector2(size.x / -2 + 0.5, size.y / 2 - 0.5)
	for y in size.x:
		for x in size.y:
			var uv: Vector2 = Vector2(x, y) / (size - Vector2.ONE)
			grid.set_pos(x, y, _anim_pos(Vector2i(x, y), uv, init_grid.get_pos(x, y)))



func _anim_pos(pos: Vector2i, uv: Vector2, init_pos: Vector3) -> Vector3:
	return init_pos
