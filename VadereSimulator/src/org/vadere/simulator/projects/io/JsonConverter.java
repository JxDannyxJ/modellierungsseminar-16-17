package org.vadere.simulator.projects.io;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.projects.ScenarioRunManager;
import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.simulator.projects.dataprocessing.DataProcessingJsonManager;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.AttributesSimulation;
import org.vadere.state.attributes.ModelDefinition;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.attributes.scenario.AttributesPedestrian;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.attributes.scenario.AttributesStairs;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.attributes.scenario.AttributesTeleporter;
import org.vadere.state.attributes.scenario.AttributesTopography;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.scenario.staticelements.Teleporter;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.GeometryUtils;
import org.vadere.util.geometry.ShapeType;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.reflection.DynamicClassInstantiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class JsonConverter {

	public static final String SCENARIO_KEY = "scenario";

	public static final String MAIN_MODEL_KEY = "mainModel";

	public static final String SUB_MODEL_KEY = "subModels";

	private static Logger logger = LogManager.getLogger(JsonConverter.class);

	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

	static {
		mapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false); // otherwise 4.7 will automatically be casted to 4 for integers, with this it throws an error
		mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION); // forbids duplicate keys
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); // to allow empty attributes like "attributes.SeatingAttr": {}, useful while in dev
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // otherwise private fields won't be usable
		// these three are to forbid deriving class variables from getters/setters, otherwise e.g. Pedestrian would have too many fields
		mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

		SimpleModule sm = new SimpleModule();

		sm.addDeserializer(boolean.class, new JsonDeserializer<Boolean>() { // make boolean parsing more strict, otherwise integers are accepted with 0=false and all other integers=true
			@Override
			public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException {
				if (!jsonParser.getCurrentToken().isBoolean())
					throw new JsonParseException(jsonParser,
							"Can't parse \"" + jsonParser.getValueAsString() + "\" as boolean");
				return jsonParser.getValueAsBoolean();
			}
		});

		sm.addDeserializer(VRectangle.class, new JsonDeserializer<VRectangle>() {
			@Override
			public VRectangle deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException {
				return deserializeVRectangle(jsonParser.readValueAsTree());
			}
		});

		sm.addSerializer(VRectangle.class, new JsonSerializer<VRectangle>() {
			@Override
			public void serialize(VRectangle vRect, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
					throws IOException {
				jsonGenerator.writeTree(serializeVRectangle(vRect));
			}
		});

		sm.addDeserializer(VShape.class, new JsonDeserializer<VShape>() {
			@Override
			public VShape deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException {
				JsonNode node = jsonParser.readValueAsTree();
				ShapeType shapeType = mapper.convertValue(node.get("type"), ShapeType.class);
				switch (shapeType) {
					case CIRCLE:
						return mapper.convertValue(node, CircleStore.class).newVCircle();
					case POLYGON:
						return mapper.convertValue(node, Polygon2DStore.class).newVPolygon();
					case RECTANGLE:
						return deserializeVRectangle(node);
					case ELLIPSE:
						return mapper.convertValue(node, EllipseStore.class).newVEllipse();
				}
				return null;
			}
		});

		sm.addSerializer(VShape.class, new JsonSerializer<VShape>() {
			@Override
			public void serialize(VShape vShape, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
					throws IOException {
				switch (vShape.getType()) {
					case CIRCLE:
						jsonGenerator.writeTree(mapper.convertValue(new CircleStore((VCircle) vShape), JsonNode.class));
						break;
					case POLYGON:
						jsonGenerator
								.writeTree(mapper.convertValue(new Polygon2DStore((VPolygon) vShape), JsonNode.class));
						break;
					case ELLIPSE:
						jsonGenerator
								.writeTree(mapper.convertValue(new EllipseStore((VEllipse) vShape), JsonNode.class));
						break;
					case RECTANGLE:
						jsonGenerator.writeTree(serializeVRectangle((VRectangle) vShape)); // this doesn't seem to get called ever, the VRectangle serializer always seem to get called
						break;
				}
			}
		});

		sm.addDeserializer(DynamicElement.class, new JsonDeserializer<DynamicElement>() {
			@Override
			public DynamicElement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException {
				JsonNode node = jsonParser.readValueAsTree();
				ScenarioElementType type = mapper.convertValue(node.get("type"), ScenarioElementType.class);
				switch (type) {
					case PEDESTRIAN:
						return mapper.convertValue(node, Pedestrian.class);
					case HORSE:
						return mapper.convertValue(node, Horse.class);
					case CAR:
						return mapper.convertValue(node, Car.class);
					// ... ?
				}
				return null;
			}
		});

		mapper.registerModule(sm);
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static <T> T deserializeObjectFromJson(String json, Class<T> objectClass) throws JsonProcessingException, IOException, TextOutOfNodeException {
		final JsonNode node = mapper.readTree(json);
		checkForTextOutOfNode(json);
		return mapper.treeToValue(node, objectClass);
	}

	// - - - - DESERIALIZING - - - -

	public static JsonNode deserializeToNode(String dev) throws IOException {
		return mapper.readTree(dev);
	}

	private static VRectangle deserializeVRectangle(JsonNode node) {
		return mapper.convertValue(node, VRectangleStore.class).newVRectangle();
	}

	private static class TopographyStore {
		AttributesTopography attributes = new AttributesTopography();
		AttributesPedestrian attributesPedestrian = new AttributesPedestrian();
		AttributesCar attributesCar = new AttributesCar();
		AttributesHorse attributesHorse = new AttributesHorse();
		Collection<AttributesObstacle> obstacles = new LinkedList<>();
		Collection<AttributesStairs> stairs = new LinkedList<>();
		Collection<AttributesTarget> targets = new LinkedList<>();
		Collection<AttributesSource> sources = new LinkedList<>();
		Collection<? extends DynamicElement> dynamicElements = new LinkedList<>();
		AttributesTeleporter teleporter = null;
	}

	private static class VRectangleStore {
		public double x;
		public double y;
		public double width;
		public double height;
		public ShapeType type = ShapeType.RECTANGLE;

		public VRectangleStore() {
		}

		public VRectangleStore(VRectangle vRect) {
			x = vRect.x;
			y = vRect.y;
			height = vRect.height;
			width = vRect.width;
		}

		public VRectangle newVRectangle() {
			return new VRectangle(x, y, width, height);
		}
	}

	private static class Polygon2DStore {
		public ShapeType type = ShapeType.POLYGON;
		public List<VPoint> points;

		public Polygon2DStore() {
		}

		public Polygon2DStore(VPolygon vPoly) {
			points = vPoly.getPoints();
		}

		public VPolygon newVPolygon() {
			return GeometryUtils.polygonFromPoints2D(points);
		}
	}

	private static class CircleStore {
		public double radius;
		public VPoint center;
		public ShapeType type = ShapeType.CIRCLE;

		public CircleStore() {
		}

		public CircleStore(VCircle vCircle) {
			radius = vCircle.getRadius();
			center = vCircle.getCenter();
		}

		public VCircle newVCircle() {
			return new VCircle(center, radius);
		}
	}

	/**
	 * Class to store informations of Ellipse and
	 * create new Instances of VEllipse.
	 */
	private static class EllipseStore {
		public double height;
		public double width;
		public VPoint center;
		public ShapeType type = ShapeType.ELLIPSE;

		public EllipseStore() {
		}

		public EllipseStore(VEllipse vEllipse) {
			height = vEllipse.getHeight();
			width = vEllipse.getWidth();
			center = vEllipse.getCenter();
		}

		public VEllipse newVEllipse() {
			return new VEllipse(center, height, width);
		}
	}

	public static ScenarioRunManager deserializeScenarioRunManager(String json) throws IOException {
		return deserializeScenarioRunManagerFromNode(mapper.readTree(json));
	}

	public static ScenarioRunManager deserializeScenarioRunManagerFromNode(JsonNode node) throws IOException {
		JsonNode rootNode = node;
		String name = rootNode.get("name").asText();
		JsonNode scenarioNode = rootNode.get(SCENARIO_KEY);
		AttributesSimulation attributesSimulation = deserializeAttributesSimulationFromNode(scenarioNode.get("attributesSimulation"));
		JsonNode attributesModelNode = scenarioNode.get("attributesModel");
		String mainModel = scenarioNode.get(MAIN_MODEL_KEY).isNull() ? null : scenarioNode.get(MAIN_MODEL_KEY).asText();
		List<MainModel> subModelNames = deserializeSubModels(scenarioNode.get(SUB_MODEL_KEY));
		List<Attributes> attributesModel = deserializeAttributesListFromNode(attributesModelNode);
		Topography topography = deserializeTopographyFromNode(scenarioNode.get("topography"));
		String description = rootNode.get("description").asText();
		ScenarioStore scenarioStore = new ScenarioStore(name, description, mainModel, attributesModel, attributesSimulation, topography, subModelNames);
		ScenarioRunManager scenarioRunManager = new ScenarioRunManager(scenarioStore);

		scenarioRunManager.setDataProcessingJsonManager(DataProcessingJsonManager.deserializeFromNode(rootNode.get(DataProcessingJsonManager.DATAPROCCESSING_KEY)));
		scenarioRunManager.saveChanges();

		return scenarioRunManager;
	}

	public static List<MainModel> deserializeSubModels(JsonNode node) throws JsonProcessingException {
		List<MainModel> subModels = new LinkedList<>();
		DynamicClassInstantiator<MainModel> instantiator = new DynamicClassInstantiator<>();
		Iterator<Map.Entry<String, JsonNode>> it = node.fields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> entry = it.next();
			MainModel subModel = mapper.treeToValue(entry.getValue(), instantiator.getClassFromName(entry.getKey()));
			subModels.add(subModel);
		}
		return subModels;
	}

	public static AttributesSimulation deserializeAttributesSimulation(String json)
			throws IOException, TextOutOfNodeException {
		return deserializeObjectFromJson(json, AttributesSimulation.class);
	}

	private static AttributesSimulation deserializeAttributesSimulationFromNode(JsonNode node)
			throws JsonProcessingException {
		return mapper.treeToValue(node, AttributesSimulation.class);
	}

	public static ModelDefinition deserializeModelDefinition(String json) throws Exception {
		JsonNode node = mapper.readTree(json);
		checkForTextOutOfNode(json);
		if (!node.has(MAIN_MODEL_KEY))
			throw new Exception("No " + MAIN_MODEL_KEY + "-entry was found.");
		String mainModelString = null;
		JsonNode mainModel = node.get(MAIN_MODEL_KEY);
		if (!mainModel.isNull()) { // avoid test-instantiating when mainModel isn't set, otherwise user has invalid json when creating a new scenario
			DynamicClassInstantiator<MainModel> instantiator = new DynamicClassInstantiator<>();
			mainModelString = mainModel.asText();
			// instantiate to get an error if the string can't be mapped onto a model
			@SuppressWarnings("unused")
			MainModel dummyToProvokeClassCast = instantiator.createObject(mainModelString);
		}
		return new ModelDefinition(mainModelString, deserializeAttributesListFromNode(node.get("attributesModel")));
	}

	public static List<Attributes> deserializeAttributesListFromNode(JsonNode node) throws JsonProcessingException {
		DynamicClassInstantiator<Attributes> instantiator = new DynamicClassInstantiator<>();
		List<Attributes> attributesList = new LinkedList<>();
		Iterator<Map.Entry<String, JsonNode>> it = node.fields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> entry = it.next();
			Attributes attributes = mapper.treeToValue(entry.getValue(), instantiator.getClassFromName(entry.getKey()));
			attributesList.add(attributes);
		}
		return attributesList;
	}

	public static Topography deserializeTopography(String json) throws IOException, TextOutOfNodeException {
		checkForTextOutOfNode(json);
		return deserializeTopographyFromNode(mapper.readTree(json));
	}

	private static Topography deserializeTopographyFromNode(JsonNode node) {
		TopographyStore store = mapper.convertValue(node, TopographyStore.class);
		Topography topography = new Topography(store.attributes, store.attributesPedestrian, store.attributesCar, store.attributesHorse);
		store.obstacles.forEach(obstacle -> topography.addObstacle(new Obstacle(obstacle)));
		store.stairs.forEach(stairs -> topography.addStairs(new Stairs(stairs)));
		store.targets.forEach(target -> topography.addTarget(new Target(target)));
		store.sources.forEach(source -> topography.addSource(new Source(source)));
		store.dynamicElements.forEach(element -> topography.addInitialElement(element));
		if (store.teleporter != null)
			topography.setTeleporter(new Teleporter(store.teleporter));
		return topography;
	}

	private static void checkForTextOutOfNode(String json) throws TextOutOfNodeException, IOException { // via stackoverflow.com/a/26026359
		JsonParser jp = mapper.getFactory().createParser(json);
		mapper.readValue(jp, JsonNode.class);
		try {
			if (jp.nextToken() != null)
				throw new TextOutOfNodeException();
		} catch (IOException e) { // can be thrown in nextToken()
			throw new TextOutOfNodeException();
		} finally {
			jp.close();
		}
	}

	public static Agent deserializeDynamicElement(String json, ScenarioElementType type) throws IOException {
		switch (type) {
			case PEDESTRIAN:
				return mapper.readValue(json, Pedestrian.class);
			case HORSE:
				return mapper.readValue(json, Horse.class);
			case CAR:
				return mapper.readValue(json, Car.class);
			default:
				return null;
		}
	}

	/**
	 * Deserialize Json input and create the right {@link Attributes} Object.
	 *
	 * @param json representation of {@link Attributes} for {@link ScenarioElementType}.
	 * @param type which is described by Json input-
	 * @return instance of {@link Attributes}.
	 */
	public static AttributesScenarioElement deserializeScenarioElementAttributes(String json, ScenarioElementType type) throws IOException {
		// TODO [priority=low] [task=refactoring] find a better way!
		switch (type) {
			case PEDESTRIAN:
				return mapper.readValue(json, AttributesAgent.class);
			case HORSE:
				return mapper.readValue(json, AttributesHorse.class);
			case CAR:
				return mapper.readValue(json, AttributesCar.class);
			case OBSTACLE:
				return mapper.readValue(json, AttributesObstacle.class);
			case SOURCE:
				return mapper.readValue(json, AttributesSource.class);
			case TARGET:
				return mapper.readValue(json, AttributesTarget.class);
			case STAIRS:
				return mapper.readValue(json, AttributesStairs.class);
			case TELEPORTER:
				return mapper.readValue(json, AttributesTeleporter.class);
			default:
				return null;
		}
	}

	// - - - - SERIALIZING - - - -

	private static JsonNode serializeVRectangle(VRectangle vRect) {
		return mapper.convertValue(new VRectangleStore(vRect), JsonNode.class);
	}

	// could also serialize each ModelType individually and add the strings with comma and brackets
	// in between - but building a proper mini-Json-ObjectNode seems a more stable and elegant solution
	public static String serializeModelPreset(ModelDefinition modelDefinition)
			throws JsonProcessingException { // may also throw one of the instantiator's exceptions

		ObjectNode node = mapper.createObjectNode();
		node.put(MAIN_MODEL_KEY, modelDefinition.getMainModel());
		node.set("attributesModel", serializeAttributesModelToNode(modelDefinition.getAttributesList()));
		return writer.writeValueAsString(node);
	}

	// used in hasUnsavedChanges, TODO [priority=high] [task=bugfix] check if commitHashIncluded can always be false
	public static String serializeScenarioRunManager(ScenarioRunManager scenarioRunManager) {
		try {
			return serializeScenarioRunManager(scenarioRunManager, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String serializeScenarioRunManager(ScenarioRunManager scenarioRunManager, boolean commitHashIncluded)
			throws IOException {
		return writer.writeValueAsString(serializeScenarioRunManagerToNode(scenarioRunManager, commitHashIncluded));
	}

	public static JsonNode serializeScenarioRunManagerToNode(ScenarioRunManager scenarioRunManager,
															 boolean commitHashIncluded) throws IOException {
		ScenarioStore scenarioStore = scenarioRunManager.getScenarioStore();
		ObjectNode rootNode = mapper.createObjectNode();
		serializeMeta(rootNode, commitHashIncluded, scenarioStore);
		rootNode.set(DataProcessingJsonManager.DATAPROCCESSING_KEY, scenarioRunManager.getDataProcessingJsonManager().serializeToNode());
		rootNode.set(SCENARIO_KEY, serializeVadereNode(scenarioStore));
		return rootNode;
	}

	private static void serializeMeta(ObjectNode node, boolean commitHashIncluded, ScenarioStore scenarioStore) {
		node.put("name", scenarioStore.name);
		node.put("description", scenarioStore.description);
		node.put("release", HashGenerator.releaseNumber());
		if (commitHashIncluded)
			node.put("commithash", HashGenerator.commitHash());
	}

	private static ObjectNode serializeVadereNode(ScenarioStore scenarioStore) {
		ObjectNode vadereNode = mapper.createObjectNode();

		vadereNode.put(MAIN_MODEL_KEY, scenarioStore.mainModel);

		ObjectNode subModelNode = serializeSubModels(scenarioStore.subModels);
		vadereNode.set("subModels", subModelNode);

		// vadere > attributesModel
		ObjectNode attributesModelNode = serializeAttributesModelToNode(scenarioStore.attributesList);
		vadereNode.set("attributesModel", attributesModelNode);

		// vadere > attributesSimulation
		vadereNode.set("attributesSimulation", mapper.convertValue(scenarioStore.attributesSimulation, JsonNode.class));

		// vadere > topography
		ObjectNode topographyNode = serializeTopographyToNode(scenarioStore.topography);
		vadereNode.set("topography", topographyNode);

		return vadereNode;
	}

	private static ObjectNode serializeSubModels(final List<MainModel> subModels) {

		ObjectNode subModelsNode = mapper.createObjectNode();
		for (MainModel subModel : subModels) {
			JsonNode node = mapper.valueToTree(subModel);
			subModelsNode.set(subModel.getClass().getName(), node);
		}
		return subModelsNode;
	}

	private static ObjectNode serializeAttributesModelToNode(final List<Attributes> attributesList) {
		List<Pair<String, Attributes>> attributePairList = attributesListToNameObjectPairList(attributesList);

		ObjectNode attributesModelNode = mapper.createObjectNode();
		attributePairList.forEach(pair -> attributesModelNode.set(pair.getLeft(),
				mapper.convertValue(pair.getRight(), JsonNode.class)));

		return attributesModelNode;
	}

	private static List<Pair<String, Attributes>> attributesListToNameObjectPairList(List<Attributes> attributesList) {
		List<Pair<String, Attributes>> list = new ArrayList<>(attributesList.size());
		for (Attributes a : attributesList)
			list.add(Pair.of(a.getClass().getName(), a));
		return list;
	}

	private static ObjectNode serializeTopographyToNode(Topography topography) {
		ObjectNode topographyNode = mapper.createObjectNode();

		JsonNode attributesNode = mapper.convertValue(topography.getAttributes(), JsonNode.class);
		((ObjectNode) attributesNode.get("bounds")).remove("type"); // manually remove that field to match the old GSON-format, seems easier than avoiding it selectively
		topographyNode.set("attributes", attributesNode);

		ArrayNode obstacleNodes = mapper.createArrayNode();
		topography.getObstacles()
				.forEach(obstacle -> obstacleNodes.add(mapper.convertValue(obstacle.getAttributes(), JsonNode.class)));
		topographyNode.set("obstacles", obstacleNodes);

		ArrayNode stairNodes = mapper.createArrayNode();
		topography.getStairs()
				.forEach(stair -> stairNodes.add(mapper.convertValue(stair.getAttributes(), JsonNode.class)));
		topographyNode.set("stairs", stairNodes);

		ArrayNode targetNodes = mapper.createArrayNode();
		topography.getTargets()
				.forEach(target -> targetNodes.add(mapper.convertValue(target.getAttributes(), JsonNode.class)));
		topographyNode.set("targets", targetNodes);

		ArrayNode sourceNodes = mapper.createArrayNode();
		topography.getSources()
				.forEach(source -> sourceNodes.add(mapper.convertValue(source.getAttributes(), JsonNode.class)));
		topographyNode.set("sources", sourceNodes);

		ArrayNode dynamicElementNodes = mapper.createArrayNode();
		topography.getPedestrianDynamicElements().getInitialElements()
				.forEach(ped -> dynamicElementNodes.add(mapper.convertValue(ped, JsonNode.class))); // TODO [priority=medium] [task=check] initial elements is the right list, isn't it?
		topography.getCarDynamicElements().getInitialElements()
				.forEach(car -> dynamicElementNodes.add(mapper.convertValue(car, JsonNode.class))); // TODO [priority=medium] [task=test] verify that this works
		topography.getHorseDynamicElemnets().getInitialElements()
				.forEach(horse -> dynamicElementNodes.add(mapper.convertValue(horse, JsonNode.class)));
		topographyNode.set("dynamicElements", dynamicElementNodes);

		JsonNode attributesPedestrianNode = mapper.convertValue(topography.getAttributesPedestrian(), JsonNode.class);
		topographyNode.set("attributesPedestrian", attributesPedestrianNode);
		if (attributesPedestrianNode != null)
			((ObjectNode) attributesPedestrianNode).remove("id");

		/*
		JsonNode attributesCarNode = mapper.convertValue(topography.getAttributesCar(), JsonNode.class);
		topographyNode.set("attributesCar", attributesCarNode);
		if (attributesPedestrianNode != null)
			((ObjectNode) attributesCarNode).remove("id");
		*/

		JsonNode attributesHorseNode = mapper.convertValue(topography.getAttributesHorse(), JsonNode.class);
		topographyNode.set("attributesHorse", attributesHorseNode);

		JsonNode attributesCarNode = mapper.convertValue(topography.getAttributesCar(), JsonNode.class);
		topographyNode.set("attributesCar", attributesCarNode);

		return topographyNode;
	}

	public static String serializeAttributesSimulation(AttributesSimulation attributesSimulation)
			throws JsonProcessingException {
		return writer.writeValueAsString(mapper.convertValue(attributesSimulation, JsonNode.class));
	}

	public static String serializeTopography(Topography topography) throws JsonProcessingException {
		return writer.writeValueAsString(serializeTopographyToNode(topography));
	}

	public static String serializeMainModelAttributesModelBundle(List<Attributes> attributesList, ScenarioStore scenarioStore)
			throws JsonProcessingException {
		ObjectNode node = mapper.createObjectNode();
		node.put(MAIN_MODEL_KEY, scenarioStore.mainModel);
		node.set("subModels", serializeSubModels(scenarioStore.subModels));
		node.set("attributesModel", serializeAttributesModelToNode(attributesList));
		return writer.writeValueAsString(node);
	}

	public static String serializeObject(Object object) throws JsonProcessingException {
		return writer.writeValueAsString(mapper.convertValue(object, JsonNode.class));
	}

	public static JsonNode toJsonNode(final Object object) {
		return mapper.convertValue(object, JsonNode.class);
	}

	public static String serializeJsonNode(JsonNode node) throws JsonProcessingException {
		return writer.writeValueAsString(node);
	}

	// CLONE VIA SERIALIZE -> DESERIALIZE

	public static ScenarioRunManager cloneScenarioRunManager(ScenarioRunManager original) throws IOException {
		JsonNode clone = serializeScenarioRunManagerToNode(original, false);
		return deserializeScenarioRunManagerFromNode(clone);
	}

	public static ScenarioStore cloneScenarioStore(ScenarioStore scenarioStore) throws IOException {
		JsonNode attributesSimulationNode = mapper.convertValue(scenarioStore.attributesSimulation, JsonNode.class);
		ObjectNode attributesModelNode = serializeAttributesModelToNode(scenarioStore.attributesList);
		ObjectNode topographyNode = serializeTopographyToNode(scenarioStore.topography);
		return new ScenarioStore(scenarioStore.name, scenarioStore.description, scenarioStore.mainModel,
				deserializeAttributesListFromNode(attributesModelNode),
				deserializeAttributesSimulationFromNode(attributesSimulationNode),
				deserializeTopographyFromNode(topographyNode), scenarioStore.subModels);
	}

	// MANIPULATE JSON

	public static String addAttributesModel(String attributesClassName, String json) throws IOException {
		JsonNode node = mapper.readTree(json);
		JsonNode attributesModelNode = node.get("attributesModel");
		DynamicClassInstantiator<Attributes> instantiator = new DynamicClassInstantiator<>();
		((ObjectNode) attributesModelNode).set(
				attributesClassName,
				mapper.convertValue(instantiator.createObject(attributesClassName), JsonNode.class));
		return writer.writeValueAsString(node);
	}

}
