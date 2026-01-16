@file:kotlin.js.JsModule("game/prototypes")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlin.js.JsClass
import kotlin.js.JsName

@JsName("Creep")
external val CreepClass: JsClass<Creep>

@JsName("GameObject")
external val GameObjectClass: JsClass<GameObject>

@JsName("Structure")
external val StructureClass: JsClass<Structure>

@JsName("OwnedStructure")
external val OwnedStructureClass: JsClass<OwnedStructure>

@JsName("StructureTower")
external val StructureTowerClass: JsClass<StructureTower>

@JsName("StructureSpawn")
external val StructureSpawnClass: JsClass<StructureSpawn>

@JsName("StructureWall")
external val StructureWallClass: JsClass<StructureWall>

@JsName("StructureContainer")
external val StructureContainerClass: JsClass<StructureContainer>

@JsName("Source")
external val SourceClass: JsClass<Source>

@JsName("Resource")
external val ResourceClass: JsClass<Resource>

@JsName("StructureRampart")
external val StructureRampartClass: JsClass<StructureRampart>

@JsName("ConstructionSite")
external val ConstructionSiteClass: JsClass<ConstructionSite>

@JsName("StructureExtension")
external val StructureExtensionClass: JsClass<StructureExtension>

@JsName("StructureRoad")
external val StructureRoadClass: JsClass<StructureRoad>

@JsName("Flag")
external val FlagClass: JsClass<Flag>
