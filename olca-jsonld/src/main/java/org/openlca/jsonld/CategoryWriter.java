package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class CategoryWriter implements Writer<Category> {

	private EntityStore store;
	private boolean writeContext = true;

	public CategoryWriter() {
	}

	public CategoryWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void skipContext() {
		this.writeContext = false;
	}

	@Override
	public void write(Category category) {
		if (category == null || store == null)
			return;
		if (store.contains(ModelType.CATEGORY, category.getRefId()))
			return;
		JsonObject obj = serialize(category, null, null);
		store.add(ModelType.CATEGORY, category.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(Category category, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (writeContext)
			JsonWriter.addContext(json);
		map(category, json);
		return json;
	}

	private void map(Category category, JsonObject json) {
		JsonWriter.addAttributes(category, json, store);
		ModelType modelType = category.getModelType();
		if (modelType != null)
			json.addProperty("modelType", modelType.name());
		JsonObject parentRef = Refs.put(category.getParentCategory(), store);
		json.add("parentCategory", parentRef);
	}
}