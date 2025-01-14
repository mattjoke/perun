package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.Collator;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinition;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * View and edit ApplicationFormItem
 * !! FOR USE IN INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditFormItemTabItem implements TabItem {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Application form item");

	/**
	 * Item object
	 */
	private ApplicationFormItem item;
	private ArrayList<ApplicationFormItem> otherItems;
	private JsonCallbackEvents events;

	/**
	 * Inputs
	 */
	// Basic
	private TextBox shortNameTextBox = new TextBox();
	private ListBox federationAttributes = new ListBox();
	private CheckBox requiredCheckBox = new CheckBox();
	private CheckBox updatableCheckBox = new CheckBox();
	private ListBox perunDestinationAttributeListBox = new ListBox();
	private ListBox perunSourceAttributeListBox = new ListBox();
	private ListBox hiddenBox = new ListBox();
	private ListBox disabledBox = new ListBox();
	private TextBox regexTextBox = new TextBox();
	private ListBox hiddenDependencyItemIdBox = new ListBox();
	private ListBox disabledDependencyItemIdBox = new ListBox();
	private ArrayList<CheckBox> applicationTypesCheckBoxes = new ArrayList<CheckBox>();
	private TextBox federationAttributeCustomValue = new TextBox();

	/**
	 * KEY = locale, VALUE = textbox
	 */
	private Map<String, TextArea> labelTextBoxes = new HashMap<String, TextArea>();
	private Map<String, TextArea> helpTextBoxes = new HashMap<String, TextArea>();
	private Map<String, TextArea> errorTextBoxes = new HashMap<String, TextArea>();

	/**
	 * KEY = locale, VALUE = Map<TextBox, TextBox> (key,value)
	 */
	private Map<String, Map<TextBox, TextBox>> optionsBoxes = new HashMap<String, Map<TextBox, TextBox>>();

	private TabItem tab;
	private int voId = 0;
	private int groupId = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 * @param groupId
	 * @param item
	 * @param otherItems
	 * @param events
	 */
	public EditFormItemTabItem(int voId, int groupId, ApplicationFormItem item, ArrayList<ApplicationFormItem> otherItems, JsonCallbackEvents events) {
		this.item = item;
		this.otherItems = otherItems;
		this.events = events;
		this.voId = voId;
		this.groupId = groupId;
	}

	public boolean isPrepared() {
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	/**
	 * Returns flex table with settings for the language
	 *
	 * @param locale
	 * @return
	 */
	private Widget itemTextTab(String locale) {

		ItemTexts itemTexts = item.getItemTexts(locale);
		if (itemTexts == null) {
			// create empty item texts
			JSONObject itemTextJson = new JSONObject();
			itemTextJson.put("label", new JSONString(""));
			itemTextJson.put("help", new JSONString(""));
			itemTextJson.put("errorMessage", new JSONString(""));
			itemTextJson.put("options", new JSONString(""));
		}
		item.setItemTexts(locale, itemTexts);

		TextArea labelTextBox = new TextArea();
		labelTextBoxes.put(locale, labelTextBox);

		TextArea helpTextBox = new TextArea();
		helpTextBoxes.put(locale, helpTextBox);

		TextArea errorTextBox = new TextArea();
		errorTextBoxes.put(locale, errorTextBox);

		// layout
		final FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		ft.setSize("550px", "100%");
		final FlexCellFormatter ftf = ft.getFlexCellFormatter();

		// sizes
		labelTextBox.setWidth("440px");
		helpTextBox.setWidth("440px");
		errorTextBox.setWidth("440px");

		// fill values
		labelTextBox.setText(itemTexts.getLabel());
		helpTextBox.setText(itemTexts.getHelp());
		errorTextBox.setText(itemTexts.getErrorMessage());

		// adding to table
		int row = 0;

		if ("HTML_COMMENT".equals(item.getType()) || "HEADING".equals(item.getType())) {

			Label label = new Label("Content:");
			ft.setWidget(row, 0, label);
			ft.setWidget(row, 1, labelTextBox);

			row++;
			ft.setHTML(row, 1, "HTML formatted content of form item. It spans through all columns to full form width.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");

			row++;

		} else if ("SUBMIT_BUTTON".equals(item.getType()) || "AUTO_SUBMIT_BUTTON".equals(item.getType())) {

			Label label = new Label("Label:");
			ft.setWidget(row, 0, label);
			ft.setWidget(row, 1, labelTextBox);

			row++;
			ft.setHTML(row, 1, "Label displayed on submit button.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");

			row++;

		} else {

			Label label = new Label("Label:");
			ft.setWidget(row, 0, label);
			ft.setWidget(row, 1, labelTextBox);

			row++;
			ft.setHTML(row, 1, "Label displayed to users to identify item on application form. If empty, \"Short name\" from basic settings is used as fallback.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");

			row++;

			Label helpLabel = new Label("Help:");
			ft.setWidget(row, 0, helpLabel);
			ft.setWidget(row, 1, helpTextBox);

			row++;
			ft.setHTML(row, 1, "Help text displayed to user along with input widget.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");

			row++;

			Label errorLabel = new Label("Error:");
			ft.setWidget(row, 0, errorLabel);
			ft.setWidget(row, 1, errorTextBox);

			row++;
			ft.setHTML(row, 1, "Error message displayed to user when enters wrong value.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");

		}

		// style
		for (int i = 0; i < ft.getRowCount(); i++) {
			ftf.setStyleName(i, 0, "itemName");
		}

		// box items table
		final FlexTable boxItemTable = new FlexTable();
		boxItemTable.setStyleName("inputFormFlexTable");
		boxItemTable.setWidth("550px");

		// final layout
		VerticalPanel vp = new VerticalPanel();
		vp.add(ft);

		// values for selection and combobox
		if (Arrays.asList("SELECTIONBOX", "COMBOBOX", "CHECKBOX", "RADIO").contains(item.getType())) {

			final Map<String, String> values = new HashMap<String, String>();

			// parse values from the item
			String options = itemTexts.getOptions();
			if (options != null) {
				// for each value, add key-value
				values.putAll(RegistrarFormItemGenerator.parseSelectionBox(options));
			}

			buildItemsTable(boxItemTable, values, locale);
			vp.add(boxItemTable);

		}

		vp.addStyleName("perun-table");

		// scroll panel
		ScrollPanel sp = new ScrollPanel(vp);
		sp.addStyleName("perun-tableScrollPanel");
		sp.setSize("560px", "100%");

		return sp;

	}

	private FlexTable buildItemsTable(final FlexTable boxItemTable, final Map<String, String> values, final String locale) {

		// clear before rebuild
		boxItemTable.clear(true);

		int boxRow = 0;

		// clear options boxes
		final Map<TextBox, TextBox> currentOptions = new HashMap<TextBox, TextBox>();
		optionsBoxes.put(locale, currentOptions);

		boxRow++;

		Label boxContentLabel = new Label(item.getType().substring(0, 1)+item.getType().toLowerCase().substring(1)+" options:");
		boxItemTable.setWidget(boxRow, 0, boxContentLabel);
		boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 0, "itemName");
		boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 4);

		boxRow++;

		HTML comment = new HTML("Define possible options for selection in SELECTIONBOX, COMBOBOX, CHECKBOX, RADIO widget. Empty options are not used.");
		comment.setStyleName("inputFormInlineComment");
		boxItemTable.setWidget(boxRow, 0, comment);
		boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 4);

		boxRow++;

		TabMenu menu = new TabMenu();
		//menu.setWidth("100%");
		boxItemTable.setWidget(boxRow, 0, menu);
		boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 4);
		boxRow++;

		final CustomButton sortButton = new CustomButton("Sort by label (A-Z)", SmallIcons.INSTANCE.sortAscendingIcon());
		menu.addWidget(sortButton);
		final CustomButton sortButton2 = new CustomButton("Sort by label (Z-A)", SmallIcons.INSTANCE.sortDescendingIcon());
		menu.addWidget(sortButton2);

		sortButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// OPTIONS
				Map<TextBox, TextBox> localeTextboxes = optionsBoxes.get(locale);
				Map<String, String> keyValue = new HashMap<String, String>();
				for (Map.Entry<TextBox, TextBox> textBoxes : localeTextboxes.entrySet()) {
					String key = textBoxes.getKey().getText();
					String value = textBoxes.getValue().getText();
					if (key != null && !key.equals("") && value != null && !value.equals("")) {
						keyValue.put(key.trim(), value.trim());
					}
				}

				List<String> values = new ArrayList<String>(keyValue.values());
				Collections.sort(values, Collator.getNativeComparator());

				Map<String, String> sortedValues = new HashMap<String, String>();

				for (String value : values) {
					for (String key : keyValue.keySet()) {
						if (Objects.equals(keyValue.get(key), value)) {
							sortedValues.put(key, keyValue.get(key));
						}
					}
				}

				buildItemsTable(boxItemTable, sortedValues, locale);

			}
		});

		sortButton2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// OPTIONS
				Map<TextBox, TextBox> localeTextboxes = optionsBoxes.get(locale);
				Map<String, String> keyValue = new HashMap<String, String>();
				for (Map.Entry<TextBox, TextBox> textBoxes : localeTextboxes.entrySet()) {
					String key = textBoxes.getKey().getText();
					String value = textBoxes.getValue().getText();
					if (key != null && !key.equals("") && value != null && !value.equals("")) {
						keyValue.put(key.trim(), value.trim());
					}
				}

				List<String> values = new ArrayList<String>(keyValue.values());
				Collections.sort(values, Collections.reverseOrder(Collator.getNativeComparator()));

				Map<String, String> sortedValues = new HashMap<String, String>();

				for (String value : values) {
					for (String key : keyValue.keySet()) {
						if (Objects.equals(keyValue.get(key), value)) {
							sortedValues.put(key, keyValue.get(key));
						}
					}
				}

				buildItemsTable(boxItemTable, sortedValues, locale);

			}
		});

		// for each add new row
		for (Map.Entry<String, String> entry : values.entrySet()) {

			final TextBox keyTextBox = new TextBox();
			final TextBox valueTextBox = new TextBox();

			currentOptions.put(keyTextBox, valueTextBox);

			keyTextBox.setText(entry.getKey());
			valueTextBox.setText(entry.getValue());

			boxItemTable.setHTML(boxRow, 0, "Value:");
			boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 0, "itemName");
			boxItemTable.setWidget(boxRow, 1, keyTextBox);
			boxItemTable.setHTML(boxRow, 2, "Label:");
			boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 2, "itemName");
			boxItemTable.setWidget(boxRow, 3, valueTextBox);

			boxRow++;

		}

		// button for adding new
		CustomButton addNewButton = new CustomButton(ButtonTranslation.INSTANCE.addButton(), ButtonTranslation.INSTANCE.addNewSelectionBoxItem(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			public void onClick(ClickEvent event) {

				final int r = boxItemTable.getRowCount();

				final TextBox keyTextBox = new TextBox();
				final TextBox valueTextBox = new TextBox();

				currentOptions.put(keyTextBox, valueTextBox);

				boxItemTable.insertRow(r - 1);

				boxItemTable.setHTML(r - 1, 0, "Value:");
				boxItemTable.getFlexCellFormatter().setStyleName(r - 1, 0, "itemName");
				boxItemTable.setWidget(r - 1, 1, keyTextBox);
				boxItemTable.setHTML(r - 1, 2, "Label:");
				boxItemTable.getFlexCellFormatter().setStyleName(r - 1, 2, "itemName");
				boxItemTable.setWidget(r - 1, 3, valueTextBox);

				UiElements.runResizeCommands(tab);

			}
		});
		boxItemTable.setWidget(boxRow, 0, addNewButton);
		boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 2);

		return boxItemTable;

	}

	/**
	 * Returns flex table with basic information and textboxes
	 *
	 * @return
	 */
	private Widget basicInformationTab() {

		// item application types
		ArrayList<String> itemApplicationTypes = JsonUtils.listFromJsArrayString(item.getApplicationTypes());

		// federation attributes to select from
		federationAttributes.addItem("No item selected (empty value)", "");
		federationAttributes.addItem("--- Custom value ---", "custom");
		federationAttributes.addItem("Display name", "displayName");
		federationAttributes.addItem("Common name", "cn");
		federationAttributes.addItem("Mail", "mail");
		federationAttributes.addItem("Organization", "o");
		federationAttributes.addItem("Level of Assurance (LoA)", "loa");
		federationAttributes.addItem("First name", "givenName");
		federationAttributes.addItem("Surname", "sn");
		federationAttributes.addItem("EPPN", "eppn");
		federationAttributes.addItem("IdP Category", "md_entityCategory");
		federationAttributes.addItem("IdP Affiliation", "affiliation");
		federationAttributes.addItem("EduPersonScopedAffiliation", "eduPersonScopedAffiliation");
		federationAttributes.addItem("Forwarded Affiliation from Proxy", "voPersonExternalAffiliation");
		federationAttributes.addItem("schacHomeOrganization", "schacHomeOrganization");
		federationAttributes.addItem("Login", "uid");
		federationAttributes.addItem("Alternative login name", "alternativeLoginName");

		hiddenBox.addItem("Never", "NEVER");
		hiddenBox.addItem("Always", "ALWAYS");
		hiddenBox.addItem("If prefilled (self or other item, if dependency specified)", "IF_PREFILLED");
		hiddenBox.addItem("If empty (self or other item, if dependency specified)", "IF_EMPTY");

		disabledBox.addItem("Never", "NEVER");
		disabledBox.addItem("Always", "ALWAYS");
		disabledBox.addItem("If prefilled (self or other item, if dependency specified)", "IF_PREFILLED");
		disabledBox.addItem("If empty (self or other item, if dependency specified)", "IF_EMPTY");

		disabledDependencyItemIdBox.addItem("-- Not selected --", "null");
		hiddenDependencyItemIdBox.addItem("-- Not selected --", "null");
		for (ApplicationFormItem otherItem : otherItems) {
			if (otherItem.getId() != 0 && otherItem.getId() != item.getId() && (otherItem.getType().equals("PASSWORD") ||
					otherItem.getType().equals("VALIDATED_EMAIL") ||
					otherItem.getType().equals("TEXTFIELD") ||
					otherItem.getType().equals("TEXTAREA") ||
					otherItem.getType().equals("CHECKBOX") ||
					otherItem.getType().equals("RADIO") ||
					otherItem.getType().equals("SELECTIONBOX") ||
					otherItem.getType().equals("COMBOBOX") ||
					otherItem.getType().equals("USERNAME")
			)) {
				disabledDependencyItemIdBox.addItem(otherItem.getShortname(), String.valueOf(otherItem.getId()));
				hiddenDependencyItemIdBox.addItem(otherItem.getShortname(), String.valueOf(otherItem.getId()));
			}
		}

		// application types
		GetAttributesDefinition attrDef = new GetAttributesDefinition(new JsonCallbackEvents() {
			@Override
			public void onError(PerunError error) {

				// SOURCE LIST BOX
				perunSourceAttributeListBox.clear();
				perunSourceAttributeListBox.addItem("No item selected (empty value)", "");
				if (item.getPerunSourceAttribute() != null && !item.getPerunSourceAttribute().isEmpty()) {
					// add and select returned perun dest attr
					perunSourceAttributeListBox.addItem(item.getPerunSourceAttribute(), item.getPerunSourceAttribute());
					perunSourceAttributeListBox.setSelectedIndex(1);
				}

				// DESTINATION LIST BOX
				perunDestinationAttributeListBox.clear();
				perunDestinationAttributeListBox.addItem("No item selected (empty value)", "");
				if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
					// add and select returned perun dest attr
					perunDestinationAttributeListBox.addItem(item.getPerunDestinationAttribute(), item.getPerunDestinationAttribute());
					perunDestinationAttributeListBox.setSelectedIndex(1);
				}

			}

			@Override
			public void onFinished(JavaScriptObject jso) {
				// clear
				perunSourceAttributeListBox.clear();
				perunDestinationAttributeListBox.clear();
				// set empty possibility
				perunSourceAttributeListBox.addItem("No item selected (empty value)", "");
				perunDestinationAttributeListBox.addItem("No item selected (empty value)", "");

				ArrayList<AttributeDefinition> list = JsonUtils.jsoAsList(jso);
				if (list != null && !list.isEmpty()) {
					// sort
					list = new TableSorter<AttributeDefinition>().sortByFriendlyName(list);
					for (AttributeDefinition def : list) {

						// allow ArrayList and LinkedHashMap attributes only to designated widgets, unless it was already selected
						boolean isMapOrListWidget = item.getType().equalsIgnoreCase("MAP_INPUT_BOX") ||
							item.getType().equalsIgnoreCase("LIST_INPUT_BOX");
						boolean isMapOrListAttribute = def.getType().contains("ArrayList") || def.getType().contains("LinkedHashMap");
						boolean alreadySelected = def.getName().equalsIgnoreCase(item.getPerunSourceAttribute())
							|| def.getName().equalsIgnoreCase(item.getPerunDestinationAttribute());
						
						if (!isMapOrListWidget && isMapOrListAttribute && !alreadySelected) {
							continue;
						}

						// add only member and user attributes
						if (def.getEntity().equalsIgnoreCase("user") || def.getEntity().equalsIgnoreCase("member")) {
							perunSourceAttributeListBox.addItem(def.getFriendlyName() + " (" + def.getEntity() + " / " + def.getDefinition() + ")", def.getName());
							perunDestinationAttributeListBox.addItem(def.getFriendlyName() + " (" + def.getEntity() + " / " + def.getDefinition() + ")", def.getName());
						} else if (def.getEntity().equalsIgnoreCase("vo")) {
							// source attributes can be VO too
							perunSourceAttributeListBox.addItem(def.getFriendlyName() + " (" + def.getEntity() + " / " + def.getDefinition() + ")", def.getName());
						} else if (def.getEntity().equalsIgnoreCase("group") && groupId != 0) {
							// source attributes can be Group too if form is for group
							perunSourceAttributeListBox.addItem(def.getFriendlyName() + " (" + def.getEntity() + " / " + def.getDefinition() + ")", def.getName());
						}
					}
				} else {
					// no attr def loaded, keep as it is set
					if (item.getPerunSourceAttribute() != null && !item.getPerunSourceAttribute().isEmpty()) {
						perunSourceAttributeListBox.addItem(item.getPerunSourceAttribute(), item.getPerunSourceAttribute());
					}
					if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
						perunDestinationAttributeListBox.addItem(item.getPerunDestinationAttribute(), item.getPerunDestinationAttribute());
					}
				}
				// set selected
				for (int i = 0; i < perunDestinationAttributeListBox.getItemCount(); i++) {
					// set proper value as "selected"
					if (perunDestinationAttributeListBox.getValue(i).equalsIgnoreCase(item.getPerunDestinationAttribute())) {
						perunDestinationAttributeListBox.setSelectedIndex(i);
						break;
					}
				}
				for (int i = 0; i < perunSourceAttributeListBox.getItemCount(); i++) {
					// set proper value as "selected"
					if (perunSourceAttributeListBox.getValue(i).equalsIgnoreCase(item.getPerunSourceAttribute())) {
						perunSourceAttributeListBox.setSelectedIndex(i);
						break;
					}
				}
			}

			@Override
			public void onLoadingStart() {
				perunSourceAttributeListBox.addItem("Loading...");
				perunDestinationAttributeListBox.addItem("Loading...");
			}
		});

		// layout
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		// fill values
		shortNameTextBox.setText(item.getShortname());
		boolean found = false;
		for (int i = 0; i < federationAttributes.getItemCount(); i++) {
			if (federationAttributes.getValue(i).equals(item.getFederationAttribute())) {
				federationAttributes.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if (!found && item.getFederationAttribute() != null && !item.getFederationAttribute().isEmpty()) {
			federationAttributes.setSelectedIndex(1); // custom value
			federationAttributeCustomValue.setEnabled(true);
		} else {
			federationAttributeCustomValue.setEnabled(false);
		}
		federationAttributeCustomValue.setText(item.getFederationAttribute());

		federationAttributes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (federationAttributes.getSelectedValue().equals("custom")) {
					federationAttributeCustomValue.setEnabled(true);
					federationAttributeCustomValue.setText(item.getFederationAttribute());
				} else {
					federationAttributeCustomValue.setEnabled(false);
					federationAttributeCustomValue.setText(federationAttributes.getSelectedValue());
				}

			}
		});

		for (int i = 0; i < hiddenBox.getItemCount(); i++) {
			String value = hiddenBox.getValue(i);
			if (value.equals(item.getHidden())) {
				hiddenBox.setSelectedIndex(i);
			}
		}
		for (int i = 0; i < disabledBox.getItemCount(); i++) {
			String value = disabledBox.getValue(i);
			if (value.equals(item.getDisabled())) {
				disabledBox.setSelectedIndex(i);
			}
		}
		for (int i = 0; i < hiddenDependencyItemIdBox.getItemCount(); i++) {
			if (hiddenDependencyItemIdBox.getValue(i).equals("null")) {
				if (item.getHiddenDependencyItemId() == 0) {
					hiddenDependencyItemIdBox.setSelectedIndex(i);
				}
			} else {
				int value = Integer.parseInt(hiddenDependencyItemIdBox.getValue(i));
				if (value == item.getHiddenDependencyItemId()) {
					hiddenDependencyItemIdBox.setSelectedIndex(i);
				}
			}
		}
		for (int i = 0; i < disabledDependencyItemIdBox.getItemCount(); i++) {
			if (disabledDependencyItemIdBox.getValue(i).equals("null")) {
				if (item.getDisabledDependencyItemId() == 0) {
					disabledDependencyItemIdBox.setSelectedIndex(i);
				}
			} else {
				int value = Integer.parseInt(disabledDependencyItemIdBox.getValue(i));
				if (value == item.getDisabledDependencyItemId()) {
					disabledDependencyItemIdBox.setSelectedIndex(i);
				}
			}
		}
		requiredCheckBox.setValue(item.isRequired());
		updatableCheckBox.setValue(item.isUpdatable());
		regexTextBox.setText(item.getRegex());


		for (Application.ApplicationType type : Application.ApplicationType.values()) {
			CheckBox cb = new CheckBox();
			boolean checked = itemApplicationTypes.contains(type.toString());
			cb.setValue(checked);
			applicationTypesCheckBoxes.add(cb);
		}

		// sizes
		shortNameTextBox.setWidth("200px");
		federationAttributes.setWidth("200px");
		perunSourceAttributeListBox.setWidth("300px");
		perunDestinationAttributeListBox.setWidth("300px");
		regexTextBox.setWidth("200px");

		// basic info
		int row = 0;

		Label shortNameLabel = new Label("Short name:");
		ft.setWidget(row, 0, shortNameLabel);
		ft.setWidget(row, 1, shortNameTextBox);

		row++;
		ft.setHTML(row, 1, "Internal item identification (used as fallback when you forgot to set \"Label\" for some language).");
		ftf.setStyleName(row, 1, "inputFormInlineComment");

		row++;

		Label inputLabel = new Label("Input widget:");
		ft.setWidget(row, 0, inputLabel);
		ft.setHTML(row, 1, CreateFormItemTabItem.inputTypes.get(item.getType()));

		row++;
		ft.setHTML(row, 1, "Specify what input widget is used for this item.");
		ftf.setStyleName(row, 1, "inputFormInlineComment");

		// set colspan for tops
		for (int i = 0; i < ft.getRowCount(); i++) {
			ftf.setColSpan(i, 1, 2);
		}

		row++;

		Label l = new Label("Display on application:");
		l.setTitle("");
		ft.setWidget(row, 0, l);
		ftf.setWidth(row, 0, "160px");

		Application.ApplicationType.values();

		int i = 0;
		for (Application.ApplicationType type : Application.ApplicationType.values()) {
			CheckBox cb = applicationTypesCheckBoxes.get(i);
			cb.setText(Application.getTranslatedType(type.toString()));
			if (type.equals(Application.ApplicationType.INITIAL)) {
				cb.setTitle("If checked, display form item on INITIAL application");
			} else {
				cb.setTitle("If checked, display form item on EXTENSION application");
			}
			ft.setWidget(row, i + 1, cb);
			i++;
		}

		row++;
		ft.setHTML(row, 1, "Define on which application types is this item displayed.");
		ftf.setStyleName(row, 1, "inputFormInlineComment");
		ftf.setColSpan(row, 1, 2);

		row++;

		// IF BUTTON OR COMMENT, don't show these
		if (!item.getType().equals("SUBMIT_BUTTON") && !item.getType().equals("AUTO_SUBMIT_BUTTON") && !item.getType().equals("HTML_COMMENT") && !item.getType().equals("HEADING")) {

			// load attr defs only when showed
			attrDef.retrieveData();

			Label requiredLabel = new Label("Required:");
			ft.setWidget(row, 0, requiredLabel);
			ft.setWidget(row, 1, requiredCheckBox);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "If checked, user can`t submit empty value (doesn't apply to non-editable fields).");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			if (!item.getType().equals("USERNAME") && !item.getType().equals("PASSWORD")) {
				row++;
				Label updatableLabel = new Label("Updatable:");
				ft.setWidget(row, 0, updatableLabel);
				ft.setWidget(row, 1, updatableCheckBox);
				ftf.setColSpan(row, 1, 2);

				row++;
				ft.setHTML(row, 1, "If checked, user can update the submitted value.");
				ftf.setStyleName(row, 1, "inputFormInlineComment");
				ftf.setColSpan(row, 1, 2);
			}

			row++;

			Label srcAttrLabel = new Label("Source attribute:");
			ft.setWidget(row, 0, srcAttrLabel);
			ft.setWidget(row, 1, perunSourceAttributeListBox);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "Select attribute, which will be used to pre-fill form value. You can select also VO "+(groupId != 0 ? "and group " : "")+"attributes.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			row++;

			Label destAttrLabel = new Label("Destination attribute:");
			ft.setWidget(row, 0, destAttrLabel);
			ft.setWidget(row, 1, perunDestinationAttributeListBox);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "Select attribute, where will be submitted value stored after accepting user`s application.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			row++;

			Label fedAttrLabel = new Label("Federation attribute:");
			ft.setWidget(row, 0, fedAttrLabel);
			ft.setWidget(row, 1, federationAttributes);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 0, "&nbsp;");
			ft.setWidget(row, 1, federationAttributeCustomValue);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "Select federation attribute to get pre-filed value from.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			if (!item.getType().equals("VALIDATED_EMAIL") && !item.getType().equals("TIMEZONE")) {

				row++;
				Label regexLabel = new Label("Regular expression:");
				ft.setWidget(row, 0, regexLabel);
				ft.setWidget(row, 1, regexTextBox);
				ftf.setColSpan(row, 1, 2);

				row++;
				ft.setHTML(row, 1, "Regular expression used for item value validation (before submitting by user).");
				ftf.setStyleName(row, 1, "inputFormInlineComment");
				ftf.setColSpan(row, 1, 2);

			}

			row++;
			ft.setWidget(row, 0, new Label("Hidden:"));
			ft.setWidget(row, 1, hiddenBox);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "When the item should be hidden during the submission.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setWidget(row, 0, new Label("Hidden dependency:"));
			ft.setWidget(row, 1, hiddenDependencyItemIdBox);
			ftf.setColSpan(row, 1, 2);

			row++;
			ft.setHTML(row, 1, "Other form item, which is used to decide, if this one should be hidden.");
			ftf.setStyleName(row, 1, "inputFormInlineComment");
			ftf.setColSpan(row, 1, 2);

			if (!item.getType().equals("PASSWORD")) {
				row++;
				ft.setWidget(row, 0, new Label("Disabled:"));
				ft.setWidget(row, 1, disabledBox);
				ftf.setColSpan(row, 1, 2);

				row++;
				ft.setHTML(row, 1, "When the item should be disabled during the submission.");
				ftf.setStyleName(row, 1, "inputFormInlineComment");
				ftf.setColSpan(row, 1, 2);

				row++;
				ft.setWidget(row, 0, new Label("Disabled dependency:"));
				ft.setWidget(row, 1, disabledDependencyItemIdBox);
				ftf.setColSpan(row, 1, 2);

				row++;
				ft.setHTML(row, 1, "Other form item, which is used to decide, if this one should be disabled.");
				ftf.setStyleName(row, 1, "inputFormInlineComment");
				ftf.setColSpan(row, 1, 2);
			}

		}

		// set styles
		for (int n = 0; n < ft.getRowCount(); n++) {
			ftf.setStyleName(n, 0, "itemName");
		}

		// scroll panel
		ScrollPanel sp = new ScrollPanel(ft);
		sp.addStyleName("perun-tableScrollPanel");
		sp.setSize("560px", "320px");

		sp.scrollToTop();

		return sp;
	}

	public Widget draw() {

		this.tab = this;

		this.titleWidget.setText("Edit form item: " + item.getShortname());

		// languages
		ArrayList<String> languages = item.getLocales();
		if (!Utils.getNativeLanguage().isEmpty()) {
			if (!languages.contains(Utils.getNativeLanguage().get(0))) languages.add(Utils.getNativeLanguage().get(0));
		}
		if (!languages.contains("en")) languages.add(0, "en");

		// vertical panel
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("570px");
		vp.setHeight("375px");

		// tab panel
		TabLayoutPanel tabPanel = new TabLayoutPanel(30, Unit.PX);
		tabPanel.addStyleName("smallTabPanel");
		tabPanel.setHeight("350px");

		// basic settings
		tabPanel.add(basicInformationTab(), "Basic settings");

		// for each locale add tab
		for (String locale : languages) {
			tabPanel.add(itemTextTab(locale), "Lang: " + locale);
		}

		// add menu
		final TabItem tab = this;
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveFormItem(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				//save item and reload local content
				saveItem();
				events.onFinished(item);
				// do not reload content from RPC !!
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// add tab panel to main panel
		vp.add(tabPanel);
		vp.setCellHeight(tabPanel, "350px");
		vp.setCellWidth(tabPanel, "570px");

		//session.getUiElements().resizeSmallTabPanel(tabPanel.getWidget(0), 350, 60, tab);

		vp.add(tabMenu);
		vp.setCellHeight(tabMenu, "30px");
		vp.setCellHorizontalAlignment(tabMenu, HasHorizontalAlignment.ALIGN_RIGHT);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	/**
	 * Saves the values back to the item
	 */
	protected void saveItem() {

		// TODO set only when actual change happens
		item.setEdited(true);

		// shortName is required item !!
		if (shortNameTextBox.getText() == null || (shortNameTextBox.getText().isEmpty())) {
			UiElements.generateAlert("Empty shortName", "'shortName' is required parameter and can't be empty !");
			return;
		}

		item.setFederationAttribute(federationAttributeCustomValue.getValue());

		if (perunDestinationAttributeListBox.getSelectedIndex() > 0) {
			// some value set
			item.setPerunDestinationAttribute(perunDestinationAttributeListBox.getValue(perunDestinationAttributeListBox.getSelectedIndex()));
		} else {
			// empty value set
			item.setPerunDestinationAttribute(null);
		}

		if (perunSourceAttributeListBox.getSelectedIndex() > 0) {
			// some value set
			item.setPerunSourceAttribute(perunSourceAttributeListBox.getValue(perunSourceAttributeListBox.getSelectedIndex()));
		} else {
			// empty value set
			item.setPerunSourceAttribute(null);
		}

		item.setRegex(regexTextBox.getText().trim());
		item.setRequired(requiredCheckBox.getValue());
		item.setUpdatable(updatableCheckBox.getValue());
		item.setShortname(shortNameTextBox.getText().trim());
		item.setHidden(hiddenBox.getValue(hiddenBox.getSelectedIndex()));
		item.setDisabled(disabledBox.getValue(disabledBox.getSelectedIndex()));

		String disabledDependency = disabledDependencyItemIdBox.getValue(disabledDependencyItemIdBox.getSelectedIndex());
		item.setDisabledDependencyItemId(disabledDependency.equals("null") ? 0 : Integer.parseInt(disabledDependency));

		String hiddenDependency = hiddenDependencyItemIdBox.getValue(hiddenDependencyItemIdBox.getSelectedIndex());
		item.setHiddenDependencyItemId(hiddenDependency.equals("null") ? 0 : Integer.parseInt(hiddenDependency));

		JSONArray newApplicationTypesJson = new JSONArray();
		int pointer = 0;
		int i = 0;
		for (Application.ApplicationType type : Application.ApplicationType.values()) {
			CheckBox cb = applicationTypesCheckBoxes.get(i);
			if (cb.getValue()) {
				newApplicationTypesJson.set(pointer, new JSONString(type.toString()));
				pointer++;
			}
			i++;
		}
		item.setApplicationTypes(newApplicationTypesJson.getJavaScriptObject());


		/* LANGUAGE */

		// item texts
		Map<String, ItemTexts> itemTextsMap = new HashMap<String, ItemTexts>();

		// help
		for (Map.Entry<String, TextArea> entry : helpTextBoxes.entrySet()) {
			String locale = entry.getKey();

			ItemTexts itemTexts;

			// if already
			if (itemTextsMap.containsKey(locale)) {
				itemTexts = itemTextsMap.get(locale);
			} else {
				itemTexts = new JSONObject().getJavaScriptObject().cast();
			}

			// set help
			itemTexts.setHelp(entry.getValue().getValue().trim());

			// update
			itemTextsMap.put(locale, itemTexts);
		}

		// label
		for (Map.Entry<String, TextArea> entry : labelTextBoxes.entrySet()) {
			String locale = entry.getKey();

			ItemTexts itemTexts;

			// if already
			if (itemTextsMap.containsKey(locale)) {
				itemTexts = itemTextsMap.get(locale);
			} else {
				itemTexts = new JSONObject().getJavaScriptObject().cast();
			}

			// set help
			itemTexts.setLabel(entry.getValue().getValue().trim());

			// update
			itemTextsMap.put(locale, itemTexts);
		}

		// error
		for (Map.Entry<String, TextArea> entry : errorTextBoxes.entrySet()) {
			String locale = entry.getKey();

			ItemTexts itemTexts;

			// if already
			if (itemTextsMap.containsKey(locale)) {
				itemTexts = itemTextsMap.get(locale);
			} else {
				itemTexts = new JSONObject().getJavaScriptObject().cast();
			}

			// set help
			itemTexts.setErrorMessage(entry.getValue().getValue().trim());

			// update
			itemTextsMap.put(locale, itemTexts);
		}

		// OPTIONS
		for (Map.Entry<String, Map<TextBox, TextBox>> localeTextboxes : optionsBoxes.entrySet()) {
			String locale = localeTextboxes.getKey();
			Map<String, String> keyValue = new HashMap<String, String>();

			// iterate over textboxes
			for (Map.Entry<TextBox, TextBox> textBoxes : localeTextboxes.getValue().entrySet()) {
				String key = textBoxes.getKey().getText();
				String value = textBoxes.getValue().getText();

				if (!key.equals("") && !value.equals("")) {
					keyValue.put(key.trim(), value.trim());
				}
			}

			// serialize key-value
			String options = RegistrarFormItemGenerator.serializeSelectionBox(keyValue);

			ItemTexts itemTexts;

			// if already
			if (itemTextsMap.containsKey(locale)) {
				itemTexts = itemTextsMap.get(locale);
			} else {
				itemTexts = new JSONObject().getJavaScriptObject().cast();
			}

			// set options
			itemTexts.setOptions(options);

			// update
			itemTextsMap.put(locale, itemTexts);
		}

		// FOR EACH ITEM TEXT, save it
		for (Map.Entry<String, ItemTexts> entry : itemTextsMap.entrySet()) {
			String locale = entry.getKey();
			ItemTexts itemTexts = entry.getValue();

			session.getUiElements().setLogText(itemTexts.toSource());

			// save it
			this.item.setItemTexts(locale, itemTexts);
		}

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EditFormItemTabItem other = (EditFormItemTabItem) o;
		if (voId != other.voId || groupId != other.groupId)
			return false;
		return true;
	}

	public int hashCode() {
		final int prime = 991;
		int result = 1;
		result = prime * result + 672 + voId + groupId;
		return result;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}
}
