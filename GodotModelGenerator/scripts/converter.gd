class_name Conventer
extends RefCounted


const TRIANGLE1: Array[Vector2i] = [Vector2i.ZERO, Vector2i.RIGHT, Vector2i.ONE]
const TRIANGLE2: Array[Vector2i] = [Vector2i.ZERO, Vector2i.ONE, Vector2i.DOWN]

var scale: float = 1
var generator: Generator

var vertex: PackedVector3Array
var uv: PackedVector2Array
var normal: PackedVector3Array



func run():
	vertex.clear()
	uv.clear()
	normal.clear()
	
	generator.update()
	var grid: Grid = generator.grid
	var size: Vector2i = grid.size
	var uv_size: Vector2 = size - Vector2i.ONE
	
	var pos_normals: Dictionary[Vector3, PackedVector3Array]
	for y in size.y:
		for x in size.x:
			var p: Vector3 = grid.get_pos(x, y)
			var up: bool = y > 0
			var down: bool = y < (size.y - 1)
			var left: bool = x > 0
			var rigth: bool = x < (size.x - 1)
			
			var v: PackedVector3Array
			if (up and left):
				v.append(get_normal(grid.get_pos(x, y - 1), p, grid.get_pos(x - 1, y)))
			if (up and rigth):
				v.append(get_normal(grid.get_pos(x + 1, y), p, grid.get_pos(x, y - 1)))
			if (down and left):
				v.append(get_normal(grid.get_pos(x - 1, y), p, grid.get_pos(x, y + 1)))
			if (down and rigth):
				v.append(get_normal(grid.get_pos(x, y + 1), p, grid.get_pos(x + 1, y)))
			
			pos_normals[p] = v
	
	for pos: Vector3 in pos_normals:
		var normals: PackedVector3Array = pos_normals[pos]
		var mean: Vector3
		for n: Vector3 in normals:
			mean += n
		
		mean /= normals.size()
		pos_normals[pos] = [mean.normalized()]
	
	for y in size.y - 1:
		for x in size.x - 1:
			for t in [TRIANGLE1, TRIANGLE2]:
				for add: Vector2i in t:
					var pos: Vector2i = Vector2i(x, y) + add
					var p: Vector3 = grid.get_pos_v(pos)
					uv.append(Vector2(pos) / uv_size)
					normal.append(pos_normals[p][0])
					vertex.append(p * scale)


func get_normal(p1: Vector3, p2: Vector3, p3: Vector3) -> Vector3:
	var d1: Vector3 = p1 - p2
	var d2: Vector3 = p2 - p3
	
	return d2.cross(d1).normalized()



func get_vertex_quad() -> PackedVector3Array:
	var arr: PackedVector3Array
	for i in vertex.size():
		var j: int = i % 6
		if (j == 3 or j == 4):
			continue
		arr.append(vertex[i])
	return arr


func get_uv_quad() -> PackedVector2Array:
	var arr: PackedVector2Array
	for i in uv.size():
		var j: int = i % 6
		if (j == 3 or j == 4):
			continue
		arr.append(uv[i])
	return arr


func get_normal_quad() -> PackedVector3Array:
	var arr: PackedVector3Array
	for i in normal.size():
		var j: int = i % 6
		if (j == 3 or j == 4):
			continue
		
		if j == 0:
			arr.append((normal[i] + normal[i + 3]) / 2.0)
		elif j == 2:
			arr.append((normal[i] + normal[i + 2]) / 2.0)
		else:
			arr.append(normal[i])
	return arr




func apply(mesh: ImmediateMesh):
	mesh.clear_surfaces()
	mesh.surface_begin(Mesh.PRIMITIVE_TRIANGLES)
	
	for i in vertex.size():
		mesh.surface_set_uv(uv[i])
		mesh.surface_set_normal(normal[i])
		mesh.surface_add_vertex(vertex[i])
	
	mesh.surface_end()



func apply_debug(mesh: ImmediateMesh):
	mesh.clear_surfaces()
	mesh.surface_begin(Mesh.PRIMITIVE_LINES)
	
	for i in vertex.size():
		var pos: Vector3 = vertex[i]
		var dir: Vector3 = normal[i] * 0.1
		mesh.surface_add_vertex(pos)
		mesh.surface_add_vertex(pos + dir)
	
	mesh.surface_end()
