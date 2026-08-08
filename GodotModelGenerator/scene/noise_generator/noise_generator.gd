@tool
extends Control


const SAVE_PATH = "res://noise/"
const COPY_TO: String = "/common/src/main/resources/assets/new_treasure_maps/textures/"


@export_tool_button("Update") var _update = update


@onready var result_nodes: Array[TextureRect] = [%Result, %Result2]
var temp: Array[ImageTexture]



func _notification(what: int) -> void:
	if what == NOTIFICATION_EDITOR_PRE_SAVE:
		temp.clear()
		for t: TextureRect in result_nodes:
			temp.append(t.texture)
			t.texture = null
	elif what == NOTIFICATION_EDITOR_POST_SAVE:
		for i in result_nodes.size():
			result_nodes[i].texture = temp[i]
		temp.clear()




func update():
	edit(%Noise.texture.get_image(), %Result, "noise_transparency")
	edit(%Noise2.texture.get_image(), %Result2, "noise_blackout")




func edit(img: Image, result_node: TextureRect, name: String):
	img.resize(256, 256, Image.INTERPOLATE_LANCZOS)
	
	result_node.texture = ImageTexture.create_from_image(img)
	
	var save_path: String = SAVE_PATH + name + ".png"
	var err: int = img.save_png(save_path)
	if err != OK:
		printerr("error save: " + error_string(err))
		return
	
	var path_to: String = ProjectSettings.globalize_path("res://").get_base_dir().get_base_dir()
	path_to += COPY_TO + name + ".png"
	
	err = DirAccess.copy_absolute(save_path, path_to)
	if err != OK:
		printerr("Error copy: " + error_string(err))
