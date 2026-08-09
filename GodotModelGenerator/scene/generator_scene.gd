@tool
extends Node


enum Type {
	NONE,
	ACCORDION,
	SCROLL
}


@export var scale_mesh: float = 1
@export_range(0, 1, 0.001) var animation_time: float
@export var type: Type = Type.ACCORDION: set=set_type
@export var debug: bool = false

@export_subgroup("Animation")
@export var speed: float = 0.5
@export var trans: Tween.TransitionType = Tween.TRANS_LINEAR
@export var ease: Tween.EaseType = Tween.EASE_OUT

@export_subgroup("")
@export_tool_button("Update") var _update_f = update
@export_tool_button("Init") var _init_f = init
@export_tool_button("Save") var _save_f = save
@export_tool_button("Anim") var _anim_f = animation
@export_tool_button("Anim Forward") var _anim_forward_f = animation.bind(true)
@export_tool_button("Anim Back") var _anim_back_f = animation.bind(false)


var mesh: ImmediateMesh
var debug_mesh: ImmediateMesh
var generator: Generator
var converter: Conventer = Conventer.new()
var tween: Tween
var is_animation: bool = false

var auto_update: bool
var params: Array



func _ready() -> void:
	mesh = $MeshInstance3D.mesh
	debug_mesh = $Debug.mesh
	init()


func set_type(t: Type):
	type = t
	init()



func _process(delta: float) -> void:
	if is_animation:
		update()
		return
	
	auto_update = not auto_update
	if auto_update:
		return
	
	var p: Array = [scale_mesh, animation_time, debug]
	if params == p:
		return
	
	params = p
	update()



func update():
	generator.time = animation_time
	converter.scale = scale_mesh
	
	converter.run()
	
	converter.apply(mesh)
	
	if not debug:
		debug_mesh.clear_surfaces()
		return
	
	converter.apply_debug(debug_mesh)


func init():
	match type:
		Type.ACCORDION:
			generator = AccordionGenerator.new()
		Type.SCROLL:
			generator = ScrollGenerator.new()
		_:
			generator = Generator.new()
	
	converter.generator = generator
	if mesh:
		update()



func animation(from_start: bool = animation_time < 0.5):
	if tween:
		tween.kill()
	tween = get_tree().create_tween()
	tween.set_trans(trans)
	tween.set_ease(ease)
	animation_time = 0 if from_start else 1
	is_animation = true
	tween.tween_property(self, "animation_time", 1 - animation_time, speed)
	tween.tween_callback(end_anim)


func end_anim():
	is_animation = false



func save():
	var saver: ModelSaver = ModelSaver.new()
	saver.save(converter)
