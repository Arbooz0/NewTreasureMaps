class_name Conventer
extends RefCounted


var scale: float
var generator: Generator

var vertex: PackedVector3Array
var uv: PackedVector2Array
var normal: PackedVector3Array

var pos_normals: Dictionary[Vector3, PackedVector3Array]



func run():
	vertex.clear()
	uv.clear()
	normal.clear()
	
	generator.update()
	var grid: Grid = generator.grid
	var size: Vector2i = grid.size
	var uv_size: Vector2 = size - Vector2i.ONE
	
	for y in size.y - 1:
		for x in size.x:
			for i in 2:
				var pos: Vector2i = Vector2i(x, y + (1 - i))
				uv.append(Vector2(pos) / uv_size)
				vertex.append(grid.get_pos_v(pos) * scale)
				if y > 0:
					if x == 0 and i == 0:
						dup_vertex()
		
		if y < (size.y - 1):
			dup_vertex()
	
	#var pos_normals: Dictionary[Vector3, PackedVector3Array]
	pos_normals.clear()
	
	var m: int = 1
	for i in vertex.size() - 2:
		var p1: Vector3 = vertex[i]
		var p2: Vector3 = vertex[i + 1]
		var p3: Vector3 = vertex[i + 2]
		
		var d1: Vector3 = p1 - p2
		var d2: Vector3 = p2 - p3
		
		var n: Vector3 = d2.cross(d1) * m
		n = n.normalized()
		
		for p in [p1, p2, p3]:
			if pos_normals.has(p):
				pos_normals[p].append(n)
			else:
				pos_normals[p] = [n]
		
		m *= -1
	
	for pos: Vector3 in vertex:
		var normals: PackedVector3Array = pos_normals[pos]
		var mean: Vector3
		for n: Vector3 in normals:
			mean += n
		
		mean /= normals.size()
		normal.append(mean.normalized())



func dup_vertex():
	uv.append(uv[-1])
	vertex.append(vertex[-1])


func apply(mesh: ImmediateMesh):
	mesh.clear_surfaces()
	mesh.surface_begin(Mesh.PRIMITIVE_TRIANGLE_STRIP)
	
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
		var dir: Vector3 = normal[i] * 0.3
		mesh.surface_add_vertex(pos)
		mesh.surface_add_vertex(pos + dir)
	
	#for pos: Vector3 in pos_normals:
		#for dir: Vector3 in pos_normals[pos]:
			#dir *= 0.2
			#mesh.surface_add_vertex(pos)
			#mesh.surface_add_vertex(pos + dir)
	
	mesh.surface_end()
