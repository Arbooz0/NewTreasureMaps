class_name Grid
extends RefCounted



var size: Vector2i
var grid: Array[Vector3]



func _init(_size: Vector2) -> void:
	size = _size
	grid.resize(size.x * size.y)




func set_pos(x: int, y: int, value: Vector3):
	grid.set(pos_to_index(x, y), value)


func set_pos_v(pos: Vector2i, value: Vector3):
	set_pos(pos.x, pos.y, value)



func get_pos(x: int, y: int) -> Vector3:
	return grid[pos_to_index(x, y)]


func get_pos_v(pos: Vector2i) -> Vector3:
	return get_pos(pos.x, pos.y)



func pos_to_index(x: int, y: int) -> int:
	return x + y * size.x



func fill_random_value(value: float):
	var p: Vector3 = Vector3.ONE * value
	fill_random(p * -1, p)


func fill_random(from: Vector3, to: Vector3):
	for i in grid.size():
		var pos: Vector3
		pos.x = randf_range(from.x, to.x)
		pos.y = randf_range(from.y, to.y)
		pos.z = randf_range(from.z, to.z)
		grid[i] = pos



func duplicate() -> Grid:
	var g: Grid = Grid.new(size)
	g.grid = grid.duplicate()
	return g
