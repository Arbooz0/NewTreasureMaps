@tool
extends Control


const SAVE_PATH = "res://noise/noise.png"
const COPY_TO: String = "/common/src/main/resources/assets/new_treasure_maps/textures/noise.png"


@export_tool_button("Update") var _update = update


var temp: Array



func _notification(what: int) -> void:
	if what == NOTIFICATION_EDITOR_PRE_SAVE:
		temp.resize(2)
		temp[0] = $HBox/BorderResult.texture
		temp[1] = $HBox/Result.texture
		$HBox/BorderResult.texture = null
		$HBox/Result.texture = null
		print("PRE SAVE")
	elif what == NOTIFICATION_EDITOR_POST_SAVE:
		$HBox/BorderResult.texture = temp[0]
		$HBox/Result.texture = temp[1]
		temp.clear()
		print("POST SAVE")




func update():
	var img: Image = Image.create_empty(256, 256, false, Image.FORMAT_RGBA8)
	var img_border: Image = Image.create_empty(256, 256, false, Image.FORMAT_RGBA8)
	var img_noise: Image = $HBox/Noise.texture.get_image()
	var img_noise_border: Image = $HBox/NoiseBorder.texture.get_image()
	
	for y in 256:
		for x in 256:
			var v: float = img_noise_border.get_pixel(x, y).r
			var d: float = Vector2(128, 128).distance_to(Vector2(x, y)) * 0.75
			d = max(absi(x - 128), absi(y - 128), d)
			d = min((128 - d) / 20.0, 1)
			v *= d
			v += d
			v = clampf(v, 0, 1)
			img_border.set_pixel(x, y, Color(v, v, v))
	
	for y in 256:
		for x in 256:
			img.set_pixel(x, y, img_noise.get_pixel(x, y) * img_border.get_pixel(x, y))
	
	$HBox/BorderResult.texture = ImageTexture.create_from_image(img_border)
	$HBox/Result.texture = ImageTexture.create_from_image(img)
	
	var err: int = img.save_png(SAVE_PATH)
	if err != OK:
		printerr("error save: " + error_string(err))
		return
	
	var path_to: String = ProjectSettings.globalize_path("res://").get_base_dir().get_base_dir()
	path_to += COPY_TO
	
	err = DirAccess.copy_absolute(SAVE_PATH, path_to)
	if err != OK:
		printerr("Error copy: " + error_string(err))
