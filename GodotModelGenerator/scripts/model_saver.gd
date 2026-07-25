class_name ModelSaver
extends RefCounted



const DIR_SAVE: String = "res://generated_models/"
const EXTENSION: String = "model"
const COPY_TO: String = "/common/src/main/resources/assets/new_treasure_maps/models/"




func save(converter: Conventer):
	DirAccess.make_dir_absolute(DIR_SAVE)
	
	var generator: Generator = converter.generator
	
	var filename: String = generator.get_name() + "." + EXTENSION
	var path: String = DIR_SAVE + filename
	var file: FileAccess = FileAccess.open(path, FileAccess.WRITE)
	if not file:
		printerr("Error open file to write: " + error_string(FileAccess.get_open_error()))
		return
	
	var count_frame: int = 10
	for frame in count_frame:
		generator.time = frame / float(count_frame - 1)
		converter.run()
		
		if frame == 0:
			file.store_16(converter.vertex.size())
			print("Count vertex: ", converter.vertex.size())
			
			for uv: Vector2 in converter.uv:
				file.store_8(roundi(uv.x * 255))
				file.store_8(roundi(uv.y * 255))
		
		for pos: Vector3 in converter.vertex:
			file.store_8(roundi(pos.x * 127))
			file.store_8(roundi(pos.y * 127))
			file.store_8(roundi(pos.z * 127))
		
		for pos: Vector3 in converter.normal:
			file.store_8(roundi(pos.x * 127))
			file.store_8(roundi(pos.y * 127))
			file.store_8(roundi(pos.z * 127))
	
	print("File size: %s (%s)" % [file.get_length(), String.humanize_size(file.get_length())])
	
	file.close()
	
	var path_to: String = ProjectSettings.globalize_path("res://").get_base_dir().get_base_dir()
	path_to += COPY_TO + filename
	var err: int = DirAccess.copy_absolute(path, path_to)
	if err != OK:
		printerr("Error copy: " + error_string(err))
