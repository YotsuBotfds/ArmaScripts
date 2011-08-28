package armaScripts.armaFighter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rsbot.bot.event.events.MessageEvent;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.internal.event.MessageListener;
import org.rsbot.script.internal.event.PaintListener;
import org.rsbot.script.methods.Calculations;
import org.rsbot.script.methods.GroundItems;
import org.rsbot.script.methods.Mouse;
import org.rsbot.script.methods.NPCs;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Walking;
import org.rsbot.script.methods.tabs.Inventory;
import org.rsbot.script.methods.ui.Bank;
import org.rsbot.script.methods.ui.Interfaces;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.Area;
import org.rsbot.script.wrappers.Character;
import org.rsbot.script.wrappers.GameModel;
import org.rsbot.script.wrappers.GroundItem;
import org.rsbot.script.wrappers.Item;
import org.rsbot.script.wrappers.NPC;
import org.rsbot.script.wrappers.Player;
import org.rsbot.script.wrappers.Tile;

import armaScripts.armaFighter.actionSets.MonksOfEntrana;
import armaScripts.interfaces.ArmaFighterActionSet;
import armaScripts.interfaces.Checker;
import armaScripts.utilities.Items;
import armaScripts.utilities.Teleports;
import armaScripts.wrappers.NPCWrapper;

@ScriptManifest(authors = { "Moran429" }, name = "Somewhat flexible fighter", description = "Can fight somewhat flexibly. Default is Monks of Entrana.")
public class ArmaFighter extends Script implements PaintListener, MessageListener {

	public Area targetArea;
	public Area bankArea;
	public int[] targetIds;
	public boolean inCombat;
	public int healthLowAt = 50;
	public boolean usePercentages = true;
	public NPC target;
	public int[] lootList;
	
	public int[] foodIds;//TODO	
	public int amountOfFood;//TODO
	public Teleports.Type teleport;//TODO
	public int[] itemsToKeep;//TODO
	public int[] potions;//TODO
	public int[] prayers;//TODO
	//TODO BURY BONES

	private boolean hoveredMap;
	private Filter<NPC> nextTargetFilter = new Filter<NPC>(){
		public boolean accept(NPC npc){
			return !npc.getLocation().equals(target.getLocation()) && actionSet.getNPCFilter().accept(npc);
		}
	};
	private Filter<GroundItem> groundItemFilter = new Filter<GroundItem>(){
		public boolean accept(final GroundItem item) {
			if (item != null) {
				final int iid = item.getItem().getID();
				final Tile itemLoc = item.getLocation();
				for (final int id : lootList) {
					if (id == iid && targetArea.contains(itemLoc)) {
						return true;
					}
				}
			}
			return false;
		}
	};
	private JFrame gui;
	private ArmaFighterActionSet actionSet;
	private State status = State.WAITING;
	private boolean goodToGo;

	private enum State {
		WALKING_TARGET_AREA, SEARCHING_TARGET_NPC, WALKING_TARGET_NPC, ATTACKING_TARGET_NPC, FIGHTING_TARGET_NPC,
		FIGHTING_NONTARGET_NPC, HEALTH_LOW_ACTION, DRINKING_POTION, SETTING_PRAYER, WAITING_FOR_LOOT, LOOTING, 
		INVENTORY_FULL_ACTION, WALKING_BANK_AREA, TELEPORTING, BANKING, WAITING
	}

	public enum HealthLowActions {
		CUSTOM_ACTION, EAT, TELEPORT, SHUTDOWN
	}
	
	public boolean healthLow(){
		return (usePercentages ? getHPPercent() : Integer.parseInt(Interfaces.getComponent(748, 8).getText())) < healthLowAt;
	}

	private State getState() {
		final Player player = Players.getLocal();
		final Tile loc = player.getLocation();
		final Character interacting = player.getInteracting();
		if(!healthLow()){
			if(interacting == null && target != null && status == State.ATTACKING_TARGET_NPC){
				inCombat = false;
				target = null;
			}else if(interacting != null && target == null){
				if(interacting instanceof NPC){
					final NPC targ = (NPC) interacting;
					final int npcId = targ.getID();
					for(int id : targetIds){
						if(npcId == id){
							inCombat = true;
							target = targ;
							break;
						}
					}
				}
			}
			if (inCombat && target != null) {
				if(interacting == null){
					inCombat = false;
					target = null;
				}else{
					if (interacting.getLocation().equals(target.getLocation()))
						return State.FIGHTING_TARGET_NPC;
					else
						return State.FIGHTING_NONTARGET_NPC;
				}
			}
			if(!targetArea.contains(loc)){
				target = null;
				return State.WALKING_TARGET_AREA;
			}else{
				if(!inCombat && GroundItems.getNearest(groundItemFilter) != null && !Inventory.isFull()){
					return State.LOOTING;
				}else{
					if(target == null || target.getHPPercent() == 0)
						return State.SEARCHING_TARGET_NPC;
					if(!target.isOnScreen())
						return State.WALKING_TARGET_NPC;
					else
						return State.ATTACKING_TARGET_NPC;
				}
			}
		}else{
			target = null;
			return State.HEALTH_LOW_ACTION;
		}
	}

	public boolean onStart(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndDisplayGui();
				goodToGo = true;
			}
		});
		Mouse.setSpeed(random(5, 8));
		actionSet = new MonksOfEntrana(this);
		return true;
	}

	public void onFinish(){
		gui.dispose();
	}

	private JPanel transportationPanel; //Includes Bank, teleport, Inventory, and items to keep
	private JPanel skillBoosterPanel; //Includes potions, prayer, and quickPrayer

	private JPanel areaPanel;
	private JSpinner swTileSpinner1;
	private JSpinner swTileSpinner2;
	private JButton swTileButton;
	private JSpinner neTileSpinner1;
	private JSpinner neTileSpinner2;
	private JButton neTileButton;
	private JPanel getAreaPanel(){
		if(areaPanel == null){
			areaPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			ChangeListener changeListener = new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					int swX = (Integer)swTileSpinner1.getValue();
					int swY = (Integer)swTileSpinner2.getValue();
					int neX = (Integer)neTileSpinner1.getValue();
					int neY = (Integer)neTileSpinner2.getValue();
					if(swX == 0 || swY == 0 || neX == 0 || neY == 0)
						return;
					targetArea = new Area(swX, swY, neX, neY);
				}
			};
			ActionListener actionListener = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					final Tile loc = Players.getLocal().getLocation();
					if(e.getActionCommand().equals("sw")){
						swTileSpinner1.setValue(loc.getX());
						swTileSpinner2.setValue(loc.getY());
					}else if(e.getActionCommand().equals("ne")){
						neTileSpinner1.setValue(loc.getX());
						neTileSpinner2.setValue(loc.getY());
					}else if(e.getActionCommand().equals("generate")){
						log("Generating area...");
						final Object[] wrappers = targetsModel.toArray();
						if(wrappers.length == 0){
							log(Color.red, "Please select some targets first.");
							JOptionPane.showMessageDialog(gui, "Please select some targets first.");
							((JTabbedPane)gui.getContentPane().getComponent(0)).setSelectedComponent(monstersPanel);
							return;
						}
						final String [] names = new String[wrappers.length];
						for(int i = 0; i < names.length; i++)
							names[i] = ((NPCWrapper)wrappers[i]).name;
						Area area = generateArea(names);
						if(area == null || area.getX() == 4 || area.getY() == 4)
							return;
						swTileSpinner1.setValue(area.getX());
						swTileSpinner2.setValue(area.getY());
						neTileSpinner1.setValue(area.getX() + area.getWidth());
						neTileSpinner2.setValue(area.getY() + area.getHeight());
						log("Succussfully generated area");
					}
				}
			};
			swTileSpinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
			swTileSpinner1.getModel().addChangeListener(changeListener);
			swTileSpinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
			swTileSpinner2.getModel().addChangeListener(changeListener);
			swTileButton = new JButton("Set");
			swTileButton.setActionCommand("sw");
			swTileButton.addActionListener(actionListener);
			areaPanel.add(swTileSpinner1, c);
			c.gridx += 5;
			areaPanel.add(swTileSpinner2, c);
			c.gridx += 5;
			areaPanel.add(swTileButton, c);
			c.gridx -= 10;
			c.gridy += 10;
			neTileSpinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
			neTileSpinner1.getModel().addChangeListener(changeListener);
			neTileSpinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
			neTileSpinner2.getModel().addChangeListener(changeListener);
			neTileButton = new JButton("Set");
			neTileButton.setActionCommand("ne");
			neTileButton.addActionListener(actionListener);
			areaPanel.add(neTileSpinner1, c);
			c.gridx += 5;
			areaPanel.add(neTileSpinner2, c);
			c.gridx += 5;
			areaPanel.add(neTileButton, c);
			final JButton generateAreaButton = new JButton("Generate");
			generateAreaButton.setActionCommand("generate");
			generateAreaButton.addActionListener(actionListener);
			c.gridx -= 10;
			c.gridy += 10;
			c.gridwidth = 15;
			areaPanel.add(generateAreaButton, c);
		}
		return areaPanel;
	}

	private JPanel monstersPanel;
	private JList targetIdsList;
	private DefaultListModel targetsModel;
	private JButton refreshMonstersButton;
	private JPanel getMonstersPanel(){
		if(monstersPanel == null){
			monstersPanel = new JPanel();
			targetsModel = new DefaultListModel();
			targetIdsList = new JList(targetsModel);
			final DefaultListModel monstersModel = new DefaultListModel();
			final JList monstersList = new JList(monstersModel);
			monstersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			targetIdsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			refreshMonstersList(monstersModel);
			addSelectionListeners(targetIdsList, monstersList);
			refreshMonstersButton = new JButton("Refresh Monsters");
			refreshMonstersButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					refreshMonstersList(monstersModel);
				}
			});
			final Dimension preferredSize = new Dimension(150, 275);
			final JScrollPane pane1 = new JScrollPane(monstersList);
			pane1.setPreferredSize(preferredSize);
			final JScrollPane pane2 = new JScrollPane(targetIdsList);
			pane2.setPreferredSize(preferredSize);
			monstersPanel.add(pane1);
			monstersPanel.add(pane2);
			monstersPanel.add(refreshMonstersButton);

			/*TODO FIXME
			foodComboBox = new JComboBox(new String[]{"No food", "Anchovies", "Anchovy pizza", "Baron shark", "Bass", "Bottle of wine", "Bread",
					"Cake", "Cavefish", "Cod", "Cooked chicken", "Cooked meat", "Garden pie", "Herring", "Juju gumbo",
					"Lobster", "Mackerel", "Manta ray", "Monkfish", "Pike", "Pineapple pizza", "Potato with cheese",
					"Rocktail", "Salmon", "Sardine", "Sea turtle", "Shark", "Summer pie", "Swordfish",
					"Trout", "Tuna", "Tuna potato"});
			foodComboBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					eatAtHpPercentSpinner.setEnabled(foodComboBox.getSelectedIndex() != 0);
				}
			});
			eatAtHpPercentSpinner = new JSpinner(new SpinnerNumberModel(35, 0, 100, 1));
			eatAtHpPercentSpinner.setEnabled(false);
			monstersPanel.add(foodComboBox);
			monstersPanel.add(eatAtHpPercentSpinner);
			 */
		}
		return monstersPanel;
	}

	private JPanel lootPanel;
	private DefaultListModel lootItemsListModel;
	private JPanel getLootPanel(){
		if(lootPanel == null){
			lootPanel = new JPanel(new BorderLayout());
			lootItemsListModel = new DefaultListModel();
			final JList lootItemsList = new JList(lootItemsListModel);
			lootItemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final DefaultListModel allItemsListModel = new DefaultListModel();
			final JList allItemsList = new JList(allItemsListModel);
			allItemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			addSelectionListeners(allItemsList, lootItemsList);

			final JTextField searchBar = new JTextField();
			searchBar.setForeground(Color.gray);
			searchBar.setText("Search for all items in Runescape");
			searchBar.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					if(searchBar.getText().equals("Search for all items in Runescape")){
						searchBar.setForeground(Color.black);
						searchBar.setText("");
					}
				}
				public void focusLost(FocusEvent e) {
					if(searchBar.getText().isEmpty()){
						searchBar.setForeground(Color.gray);
						searchBar.setText("Search for all items in Runescape");
					}
				}
			});
			searchBar.addKeyListener(new KeyListener(){
				public void keyTyped(KeyEvent e) {}
				public void keyPressed(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {
					final String text = searchBar.getText();
					if(text.length() >= 3)
						searchLootItems(allItemsListModel, text);
				}
			});
			searchBar.setToolTipText("Items to search.");
			lootPanel.add(searchBar, BorderLayout.NORTH);

			final Dimension d = new Dimension(220, 250);
			final JScrollPane sp1 = new JScrollPane(allItemsList);
			sp1.setPreferredSize(d);
			final JScrollPane sp2 = new JScrollPane(lootItemsList);
			sp2.setPreferredSize(d);
			final JPanel center = new JPanel();
			center.add(sp1);
			center.add(sp2);
			lootPanel.add(center, BorderLayout.CENTER);
		}
		return lootPanel;
	}

	private void searchLootItems(DefaultListModel lootModel, String search){
		int[] itemsIds = Items.getItemIDs(search);
		final ArrayList<String> itemNames = new ArrayList<String>();
		for(int itemId : itemsIds){
			final String itemName = Items.getItemName(itemId);
			if(!itemNames.contains(itemName) && !lootItemsListModel.contains(itemName))
				itemNames.add(itemName);
		}
		Object[] items = itemNames.toArray();
		Arrays.sort(items);
		lootModel.clear();
		for(Object o : items){
			lootModel.addElement(o);
		}
	}

	private void addSelectionListeners(final JList l1, final JList l2){
		final MouseListener MouseListener = new MouseAdapter(){
			public void mouseReleased(MouseEvent e) {
				if(e.getSource() == l1){
					listAction(l1, l2);
				}else if(e.getSource() == l2){
					listAction(l2, l1);
				}
			}
		};
		l1.addMouseListener(MouseListener);
		l2.addMouseListener(MouseListener);
	}

	private void listAction(JList l1, JList l2){
		final Object value = l1.getSelectedValue();
		if(value == null)
			return;
		final DefaultListModel m1 = (DefaultListModel) l1.getModel();
		final DefaultListModel m2 = (DefaultListModel) l2.getModel();
		int index = Arrays.binarySearch(m2.toArray(), value);
		if(index >= 0)
			return;
		m2.add(~index, value);
		m1.removeElement(value);
	}

	private void refreshMonstersList(DefaultListModel monstersModel){
		final Filter<NPC> attackable = new Filter<NPC>(){
			public boolean accept(NPC t) {
				if(t == null)
					return false;
				for(String action : t.getActions()){
					if(action == null){
						continue;
					}
					action = action.toLowerCase();
					if(action.contains("attack"))
						return true;
				}
				return false;
			}
		};
		final NPC[] monsters = NPCs.getLoaded(attackable);
		final ArrayList<NPCWrapper> npcWrappersList = new ArrayList<NPCWrapper>();
		for(NPC npc : monsters){
			final NPCWrapper wrapper = new NPCWrapper(npc);
			if(!npcWrappersList.contains(wrapper))
				npcWrappersList.add(wrapper);
		}
		final NPCWrapper[] npcWrappers = npcWrappersList.toArray(new NPCWrapper[npcWrappersList.size()]);
		Arrays.sort(npcWrappers);
		monstersModel.clear();
		for(NPCWrapper wrapper : npcWrappers){
			boolean isContained = false;
			for(int i = 0; i < targetsModel.getSize() && !isContained; i++)
				isContained = wrapper.equals(targetsModel.get(i));
			if(!isContained)
				monstersModel.addElement(wrapper);
		}
	}
	
	private JPanel foodPanel;
	private DefaultListModel foodIdsListModel;
	private JSpinner healthLowSpinner;
	private JSpinner amountOfFoodSpinner;
	private JRadioButton percent;
	private JPanel getFoodPanel(){
		if(foodPanel == null){
			foodPanel = new JPanel();
			
			foodIdsListModel = new DefaultListModel();
			final JList foodIdsList = new JList(foodIdsListModel);
			final DefaultListModel allFoodIdsListModel = new DefaultListModel();
			for(String food : new String[]{"Anchovies", "Anchovy pizza", "Baron shark", "Bass", "Bottle of wine", "Bread",
				"Cake", "Cavefish", "Cod", "Cooked chicken", "Cooked meat", "Garden pie", "Herring", "Juju gumbo",
				"Lobster", "Mackerel", "Manta ray", "Monkfish", "Pike", "Pineapple pizza", "Potato with cheese",
				"Rocktail", "Salmon", "Sardine", "Sea turtle", "Shark", "Summer pie", "Swordfish",
				"Trout", "Tuna", "Tuna potato"})
				allFoodIdsListModel.addElement(food);
			final JList allFoodsList = new JList(allFoodIdsListModel);
			allFoodsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			addSelectionListeners(foodIdsList, allFoodsList);
			
			final SpinnerNumberModel healthLowSpinnerModel = new SpinnerNumberModel(35, 0, 99, 1);
			healthLowSpinner = new JSpinner(healthLowSpinnerModel);
			amountOfFoodSpinner = new JSpinner(new SpinnerNumberModel(12, 0, 28, 1));
			percent = new JRadioButton("Percent", true);
			final JRadioButton hitpoints = new JRadioButton("Hitpoints");
			final ButtonGroup group = new ButtonGroup();
			group.add(percent);
			group.add(hitpoints);
			healthLowSpinnerModel.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					final boolean enabled = (Integer)healthLowSpinner.getValue() > 0;
					foodIdsList.setEnabled(enabled);
					allFoodsList.setEnabled(enabled);
					percent.setEnabled(enabled);
					hitpoints.setEnabled(enabled);
					amountOfFoodSpinner.setEnabled(enabled);
				}
			});
			final ActionListener listener = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					if(e.getSource() == percent){
						int i = (Integer)healthLowSpinner.getValue();
						if(i > 99)
							healthLowSpinner.setValue(99);
						healthLowSpinnerModel.setMaximum(99);
					}else if(e.getSource() == hitpoints){
						healthLowSpinnerModel.setMaximum(990);
					}
				}
			};
			percent.addActionListener(listener);
			hitpoints.addActionListener(listener);
			
			final Dimension d = new Dimension(150, 275);
			final JScrollPane sp1 = new JScrollPane(allFoodsList);
			sp1.setPreferredSize(d);
			final JScrollPane sp2 = new JScrollPane(foodIdsList);
			sp2.setPreferredSize(d);
			
			foodPanel.add(sp1);
			foodPanel.add(sp2);
			JPanel eastOuter = new JPanel(new BorderLayout());
			JPanel eastInner = new JPanel(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();
			eastInner.add(new JLabel("Health low at:"), c);
			c.gridx += 5;
			eastInner.add(percent, c);
			c.gridy += 5;
			c.gridx -= 5;
			eastInner.add(healthLowSpinner, c);
			c.gridx += 5;
			eastInner.add(hitpoints, c);
			final JPanel south = new JPanel(new BorderLayout());
			south.add(new JLabel("Amount of food to withdraw"), BorderLayout.CENTER);
			south.add(amountOfFoodSpinner, BorderLayout.SOUTH);
			eastOuter.add(eastInner, BorderLayout.NORTH);
			eastOuter.add(new JSeparator(), BorderLayout.CENTER);
			eastOuter.add(south, BorderLayout.SOUTH);
			foodPanel.add(eastOuter);
		}
		return foodPanel;
	}

	private void createAndDisplayGui(){
		if(gui == null){
			gui = new JFrame("ArmaFighter");
			gui.setSize(1000, 1000);
			final JPanel contentPane = new JPanel(new BorderLayout());
			final JTabbedPane tp = new JTabbedPane();
			final JButton start = new JButton("START");
			start.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
				}
			});
			contentPane.add(tp, BorderLayout.CENTER);
			contentPane.add(start, BorderLayout.SOUTH);
			tp.add("Targets", new JScrollPane(getMonstersPanel()));
			tp.add("Area", new JScrollPane(getAreaPanel()));
			tp.add("Food", new JScrollPane(getFoodPanel()));
			tp.add("Loot", new JScrollPane(getLootPanel()));
			gui.setContentPane(contentPane);
			gui.pack();
			gui.setLocationRelativeTo(null);
			gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			gui.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					if(loadSettings())
						gui.setVisible(false);
					else
						log(Color.red, "Failed to load settings. Please check the gui again.");
				}
			});
		}
		gui.setVisible(true);
	}

	private Area generateArea(Object[] identifiers){
		NPC[] targets = null;
		if(identifiers[0] instanceof String){
			targets = NPCs.getLoaded((String[])identifiers);
		}else if(identifiers[0] instanceof Integer){
			int arr[] = new int[identifiers.length];
			for(int i = 0; i < arr.length; i++)
				arr[i] = (Integer) identifiers[i];
			targets = NPCs.getLoaded(arr);
		}
		if(targets == null)
			return null;
		int swXmin = 0;
		int swYmin = 0;
		int neXmax = 0;
		int neYmax = 0;
		for(NPC target : targets){
			final Tile tile = target.getLocation();
			if(Calculations.distanceTo(tile) > 50 || tile.getX() == 7 || tile.getY() == 7)
				//Apparently 7 is the magic number of npc locations...
				continue;
			if(tile.getX() < swXmin || swXmin == 0)
				swXmin = tile.getX();
			else if(tile.getX() > neXmax)
				neXmax = tile.getX();
			if(tile.getY() < swYmin || swYmin == 0)
				swYmin = tile.getY();
			else if(tile.getY() > neYmax)
				neYmax = tile.getY();
		}
		if(swXmin == 0)
			return null;
		return new Area(swXmin - 3, swYmin - 3, neXmax + 3, neYmax + 3);
	}
	
	private boolean canAttack(NPC npc){
		for(String action : npc.getActions()){
			if(action == null)
				continue;
			if(action.equals("Attack"))
				return true;
		}
		return false;
	}

	private boolean loadSettings(){

		//IDS
		Object[] wrappers = targetsModel.toArray();
		final int[] ids = new int[wrappers.length];
		for(int i = 0; i < ids.length; i++)
			ids[i] = ((NPCWrapper)wrappers[i]).id;
		if(ids.length == 0)
			return false;

		//AREA
		if(targetArea == null || targetArea.getX() == 0 || targetArea.getY() == 0 || targetArea.getX() + targetArea.getWidth() == 0 || targetArea.getY() + targetArea.getHeight() == 0)
			return false;

		//LOOTING
		final HashSet<Integer> lootSet = new HashSet<Integer>();
		for(Object item : lootItemsListModel.toArray()){
			for(int id : Items.getItemIDsExactString((String)item)){
				lootSet.add(id);
			}
		}
		final Integer[] lootListInteger = lootSet.toArray(new Integer[lootSet.size()]);
		
		//FOOD
		final int[] foodListIds = new int[foodIdsListModel.size()];
		for(int i = 0; i < foodListIds.length; i++)
			foodListIds[i] = (Integer) foodIdsListModel.get(i);

		//SETTING THE SETTINGS
		targetIds = ids;
		lootList = new int[lootListInteger.length];
		for(int i = 0; i < lootListInteger.length; i++)
			lootList[i] = lootListInteger[i];
		foodIds = foodListIds;
		healthLowAt = (Integer) healthLowSpinner.getValue();
		usePercentages = percent.isSelected();
		amountOfFood = (Integer) amountOfFoodSpinner.getValue();
		bankArea = new Area(-1, -1, 1, 1); //TODO FIXME
		log("Succussfully set settings");
		return true;
	}

	@Override
	public int loop() {
		if(!goodToGo || gui.isVisible()){
			return 500;
		}
		if(Walking.getEnergy() > random(25, 60) && !Walking.isRunEnabled())
			Walking.setRun(true);
		status = getState();
		log(status);
		return loop(status);
	}

	private void searchNearestTarget(){
		NPC nearest = null;
		int dist = Integer.MAX_VALUE;
		for(NPC npc : NPCs.getLoaded(actionSet.getNPCFilter())){
			final int d = Calculations.distanceTo(npc.getLocation());
			if(d < dist){
				nearest = npc;
				dist = d;
			}
		}
		target = nearest;
	}

	public boolean waitFor(Checker conditionMet, long timeout){
		final long startTime = System.currentTimeMillis();
		while(!conditionMet.conditionMet() && System.currentTimeMillis() - startTime <= timeout)
			sleep(500);
		return conditionMet.conditionMet();
	}

	public int loop(State state){
		if(state != State.FIGHTING_TARGET_NPC && hoveredMap)
			hoveredMap = false;
		switch(state){
		case WALKING_TARGET_AREA:
			if(!actionSet.walkToTargets()){

				return 300;
			}
			break;
		case WALKING_TARGET_NPC:
			if(!actionSet.walkToTarget()){

				return 300;
			}
			if(target != null && target.getInteracting() == null)
				break;
		case SEARCHING_TARGET_NPC:
			searchNearestTarget();
			break;
		case ATTACKING_TARGET_NPC:
			if(!target.interact("Attack")){

				return 500;
			}
			waitForCombat();
			if(!inCombat){

				searchNearestTarget();
			}
			break;
		case FIGHTING_NONTARGET_NPC:
			final Character t = Players.getLocal().getInteracting();
			if(t instanceof NPC){
				if(!canAttack((NPC)t))
					break;
			}
			if(targetArea.contains(Players.getLocal().getLocation())){
				return loop(State.WALKING_BANK_AREA);
			}
			break;
		case HEALTH_LOW_ACTION:
			if(!actionSet.healthLow()){
				
			}
			break;
		case DRINKING_POTION:

			break;
		case SETTING_PRAYER:

			break;
		case WAITING_FOR_LOOT:

			break;
		case LOOTING:
			final GroundItem item = GroundItems.getNearest(groundItemFilter);
			if(item == null)
				break;
			if(!actionSet.getWalker().walkTo(item.getLocation()))
				return 500;
			if(item.interact("Take", item.getItem().getName())){
				if(Players.getLocal().getLocation().equals(item.getLocation()))
					return 250;
				if(!waitFor(new Checker(){
					public boolean conditionMet(){
						return Players.getLocal().isMoving();
					}
				}, 5000))
					return 500;
				waitFor(new Checker(){
					public boolean conditionMet(){
						return Players.getLocal().getLocation().equals(item.getLocation());
					}
				}, 5000);
			}
			break;
		case INVENTORY_FULL_ACTION:
			if(!actionSet.inventoryFull()){

			}
			break;
		case WALKING_BANK_AREA:
			if(!actionSet.walkToBank()){

			}
			break;
		case BANKING:
			if(!Bank.open())
				return 200;
			if(!Bank.depositAllExcept(itemsToKeep))
				return 200;
			Bank.close();
			break;
		case TELEPORTING:
			if(!Teleports.teleport(teleport))
				return loop(State.WALKING_BANK_AREA);
			break;
		case FIGHTING_TARGET_NPC:
			Character c = Players.getLocal().getInteracting();
			if(c == null || c.getHPPercent() == 0){
				inCombat = false;
				return 0;
			}
			NPC npc = NPCs.getNearest(nextTargetFilter);
			if(npc != null){
				if(npc.isOnScreen()){
					final GameModel model = npc.getModel();
					if(model != null && !model.contains(Mouse.getLocation()))
						Mouse.move(model.getPoint());
				}else if(!hoveredMap && Calculations.canReach(npc.getLocation(), false)){
					final Tile loc = npc.getLocation();
					Mouse.move(Calculations.worldToMinimap(loc.getX(), loc.getY()), 3, 3);
					hoveredMap = true;
				}
			}
			return random(10, 200);
		default:
			return 200;	
		}
		return 200;
	}

	public int getHPPercent(){
		return Integer.parseInt(Interfaces.getComponent(748, 8).getText())*10/Skills.getLevel(Skills.CONSTITUTION);
	}

	public boolean eatFood(){
		final Item item = Inventory.getItem(foodIds);
		if(item == null){
			actionSet.walkToBank();
			stop();
			return false;
		}
		final int count = Inventory.getCount(item.getID());
		if(item.click(true)){
			final Timer timer = new Timer(3000);
			boolean eaten = false;
			while(timer.isRunning() && !eaten){
				eaten = Inventory.getCount(item.getID()) < count;
				sleep(750);
			}
			return eaten;
		}
		return false;
	}

	private void waitForCombat(){
		Timer timer = new Timer(5000);
		while(timer.isRunning() || Players.getLocal().isMoving()){
			if(target.getInteracting() != null){
				if(Players.getLocal().getInteracting() != null){
					inCombat = true;
					return;
				}else{
					target = null;
					return;
				}
			}
		}
	}
	
	private HashMap<Point, Long> clickedPoints = new HashMap<Point, Long>();
	private void drawMouse(Graphics g){
		g.setColor(new Color(255, 140, 0, 180));
		Point loc = Mouse.getLocation();
		g.fillRect(loc.x - 7, loc.y - 1, 15, 3);
		g.fillRect(loc.x - 1, loc.y - 7, 3, 15);
		g.drawLine(loc.x - 8, loc.y, loc.x - 8, loc.y);
		g.drawLine(loc.x + 8, loc.y, loc.x + 8, loc.y);
		g.drawLine(loc.x, loc.y - 8, loc.x, loc.y - 8);
		g.drawLine(loc.x, loc.y + 8, loc.x, loc.y + 8);
		final long time = System.currentTimeMillis() - Mouse.getPressTime();
		if(time >= 0 && time <= 1500)
			clickedPoints.put(Mouse.getPressLocation(), Mouse.getPressTime());
		for(Point point : clickedPoints.keySet()){
			if(System.currentTimeMillis() - clickedPoints.get(point) > 1500){
				clickedPoints.remove(point);
				continue;
			}
			g.fillRect(point.x - 5, point.y - 5, 4, 4);
			g.fillRect(point.x + 2, point.y - 5, 4, 4);
			g.fillRect(point.x + 2, point.y + 2, 4, 4);
			g.fillRect(point.x - 5, point.y + 2, 4, 4);
		}
	}

	public void onRepaint(Graphics g){
		actionSet.paint(g);
		drawMouse(g);
	}

	public void messageReceived(MessageEvent e) {
		if(e.getMessage().contains("reached level")){
			if(Skills.getLevel(Skills.ATTACK) == 40)
				stop();
		}
		if(e.getMessage().equals("Someone else if fighting that."))
			target = null;
	}

}
