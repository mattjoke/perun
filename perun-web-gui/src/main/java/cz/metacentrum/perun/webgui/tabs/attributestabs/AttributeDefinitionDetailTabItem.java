package cz.metacentrum.perun.webgui.tabs.attributestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.attributesManager.ConvertAttributeToNonUnique;
import cz.metacentrum.perun.webgui.json.attributesManager.ConvertAttributeToUnique;
import cz.metacentrum.perun.webgui.json.attributesManager.UpdateAttribute;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServicesByAttrDefinition;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.servicestabs.ServiceDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Arrays;
import java.util.Objects;

/**
 * Detail of Attribute definition
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AttributeDefinitionDetailTabItem implements TabItem {

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
	private Label titleWidget = new Label("Attribute");

	private AttributeDefinition def;

	/**
	 * Creates a tab instance
	 */
	public AttributeDefinitionDetailTabItem(AttributeDefinition def){
		this.def = def;
	}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText(def.getName());
		final TabItem tab = this;

		// create main panel for content
		final FlexTable mainPage = new FlexTable();
		mainPage.setWidth("100%");

		final ExtendedTextBox description = new ExtendedTextBox();
		description.setWidth("100%");
		description.getTextBox().setText(def.getDescription());
		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (description.getTextBox().getText().trim().isEmpty()) {
					description.setError("Description can't be empty.");
					return false;
				}
				description.setOk();
				return true;
			}
		};

		final ExtendedTextBox displayName = new ExtendedTextBox();
		displayName.setWidth("100%");
		displayName.getTextBox().setText(def.getDisplayName());
		final ExtendedTextBox.TextBoxValidator validatorName = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (displayName.getTextBox().getText().trim().isEmpty()) {
					displayName.setError("Display name can't be empty.");
					return false;
				}
				displayName.setOk();
				return true;
			}
		};

		description.setValidator(validator);
		displayName.setValidator(validatorName);

		CheckBox unique = new CheckBox();
		unique.setValue(def.isUnique());

		if (Arrays.asList("core","virt").contains(def.getDefinition()) || def.getEntity().equals("entityless")) {

			unique.setEnabled(false);

		} else {

			unique.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {

					if (valueChangeEvent.getValue()) {
						UiElements.generateAlert("Change confirmation", "Changing attribute to UNIQUE might take a lot of time if there is large number of entities with set values. Perun will check uniqueness during the process. If values are not unique, conversion will be stopped.<p style=\"color:red;\">We strongly recommend to refresh whole browser window after conversion is DONE to prevent errors when modifying attributes from GUI.", new ClickHandler() {
							@Override
							public void onClick(ClickEvent clickEvent) {
								ConvertAttributeToUnique convert = new ConvertAttributeToUnique(new JsonCallbackEvents() {
									@Override
									public void onFinished(JavaScriptObject jso) {
										unique.setValue(true);
										unique.setEnabled(true);
										def.setUnique(true);
									}

									@Override
									public void onError(PerunError error) {
										unique.setValue(false);
										unique.setEnabled(true);
										def.setUnique(false);
									}

									@Override
									public void onLoadingStart() {
										unique.setEnabled(false);
									}
								});
								convert.convertAttributeDefinitionToUnique(def.getId());
							}
						}, new ClickHandler() {
							@Override
							public void onClick(ClickEvent clickEvent) {
								// action canceled
								unique.setValue(false);
							}
						});
					} else {
						ConvertAttributeToNonUnique convert = new ConvertAttributeToNonUnique(new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								unique.setValue(false);
								unique.setEnabled(true);
								def.setUnique(false);
							}

							@Override
							public void onError(PerunError error) {
								unique.setValue(true);
								unique.setEnabled(true);
								def.setUnique(true);
							}

							@Override
							public void onLoadingStart() {
								unique.setEnabled(false);
							}
						});
						convert.convertAttributeDefinitionToNonUnique(def.getId());
					}

				}
			});


		}

		FlexTable attributeDetailTable = new FlexTable();
		attributeDetailTable.setStyleName("inputFormFlexTable");


		final CustomButton updateButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save attribute details");
		updateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ((!Objects.equals(def.getDescription(), description.getTextBox().getText().trim()) || !Objects.equals(def.getDisplayName(), displayName.getTextBox().getText().trim()))) {

					if (!validator.validateTextBox() || !validatorName.validateTextBox()) return;

					def.setDescription(description.getTextBox().getText().trim());
					def.setDisplayName(displayName.getTextBox().getText().trim());

					UpdateAttribute request = new UpdateAttribute(JsonCallbackEvents.disableButtonEvents(updateButton));
					request.updateAttribute(def);
				}
			}
		});

		attributeDetailTable.setHTML(0, 0, "Display name:");
		attributeDetailTable.setWidget(0, 1, displayName);
		attributeDetailTable.setHTML(1, 0, "Description:");
		attributeDetailTable.setWidget(1, 1, description);
		attributeDetailTable.setHTML(2, 0, "Unique:");
		attributeDetailTable.setWidget(2, 1, unique);
		for (int i=0; i<attributeDetailTable.getRowCount(); i++) {
			attributeDetailTable.getFlexCellFormatter().setStyleName(i, 0, "itemName");
		}
		String newGuiAlertContent = session.getNewGuiAlert();
		final FlexTable alert = new FlexTable();
		String alertText = "<p>Setting attribute rights is no longer supported in this GUI. In order to set attribute rights please use the New GUI.</p> ";
		if (newGuiAlertContent != null && !newGuiAlertContent.isEmpty()) alertText += newGuiAlertContent;
		alert.setHTML(0,0, alertText);

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(updateButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// create new instance for jsonCall
		final GetServicesByAttrDefinition services = new GetServicesByAttrDefinition(def.getId());
		services.setCheckable(false);

		CellTable<Service> attrDefTable = services.getTable(new FieldUpdater<Service, String>() {
			@Override
			public void update(int index, Service object, String value) {
				session.getTabManager().addTab(new ServiceDetailTabItem(object));
			}
		});
		attrDefTable.setStyleName("perun-table");
		ScrollPanel scrollTable = new ScrollPanel(attrDefTable);
		scrollTable.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(scrollTable, 350, this);

		// set content to page

		mainPage.setWidget(0, 0, menu);
		mainPage.getFlexCellFormatter().setColSpan(0, 0, 2);

		mainPage.setWidget(1, 0, attributeDetailTable);
		mainPage.setWidget(1, 1, alert);
		mainPage.getFlexCellFormatter().setWidth(1, 0, "50%");
		mainPage.getFlexCellFormatter().setWidth(1, 1, "50%");

		HTML title = new HTML("<p>Required by service</p>");
		title.setStyleName("subsection-heading");
		mainPage.setWidget(2, 0, title);
		mainPage.getFlexCellFormatter().setColSpan(2, 0, 2);

		// put page into scroll panel
		mainPage.setWidget(3, 0, scrollTable);
		mainPage.getFlexCellFormatter().setColSpan(3, 0, 2);
		mainPage.getFlexCellFormatter().setHeight(3, 0, "100%");

		this.contentWidget.setWidget(mainPage);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 557;
		int result = 1;
		result = prime * result * 13;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {

		return session.isPerunAdmin();

	}

}
