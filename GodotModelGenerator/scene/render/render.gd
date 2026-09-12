extends Node3D




var animation_time: float = 1
var generator: Generator = ScrollGenerator.new()
var converter: Conventer = Conventer.new()

@onready var mesh_inst: MeshInstance3D = $MeshInstance3D
@onready var mesh: Mesh = mesh_inst.mesh



func _ready() -> void:
	converter.generator = generator
	converter.scale = generator._get_scale()
	mesh_inst.rotation.y = 0.2
	
	update()
	
	$AnimationPlayer.play("anim")
	
	#var tween: Tween = get_tree().create_tween()
	#tween.tween_property(mesh_inst, "rotation:y", -0.1, 2) \
	#.set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	#tween.tween_property(mesh_inst, "rotation:y", 0, 1.1) \
	#.set_trans(Tween.TRANS_LINEAR).set_ease(Tween.EASE_OUT)
	
	await get_tree().create_timer(4).timeout
	get_tree().quit()



func _process(delta: float) -> void:
	if animation_time < 0:
		return
	
	animation_time = clampf(animation_time - (delta * 2), 0, 1)
	
	update()



func update():
	mesh_inst.position.y = animation_time * -0.8
	generator.time = animation_time
	
	converter.run()
	
	converter.apply(mesh)
