package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Translucent, draggable module windows with a compact settings popover. */
final class ControlPanel extends Screen {
    private static final int GAP = 10, HEADER = 29;
    private static final long OPEN_NANOS = 440_000_000L, CLOSE_NANOS = 170_000_000L;
    private static final EnumMap<Feature.Group, List<Feature>> MODULES = new EnumMap<>(Feature.Group.class);

    static {
        for (Feature.Group group : Feature.Group.values()) {
            MODULES.put(group, Arrays.stream(Feature.values()).filter(feature -> feature.group == group).toList());
        }
    }

    private final Screen parent;
    private final boolean settingsEntry;
    private final EnumMap<Feature.Group,List<Feature>> filteredModules=new EnumMap<>(Feature.Group.class);
    private long filteredRevision=-1;
    private long lastFrame=System.nanoTime();
    private float frameSeconds;
    private final EnumMap<Feature,Float> rowHover=new EnumMap<>(Feature.class);
    private final EnumMap<Feature.Group, int[]> positions = new EnumMap<>(Feature.Group.class);
    private final EnumMap<Feature.Group, Integer> scrolls = new EnumMap<>(Feature.Group.class);
    private final EnumSet<Feature.Group> collapsed = EnumSet.noneOf(Feature.Group.class);
    private final List<Feature.Group> zOrder = new ArrayList<>();
    private Feature detail;
    private boolean uiSettings;
    private String query="";
    private boolean activeOnly;
    private EditBox search;
    private PanelButton clearSearch;
    private String hoverHint = "";
    private long hoverSince;
    private float viewScale = .7f;
    private Screen destination;
    private List<Settings.Option> detailOptions = List.of(), shownOptions = List.of();
    private int columns, columnWidth, rowHeight, homeTop, maxRows;
    private int workspaceWidth, workspaceHeight, workspaceBottom;
    private int dialogX, dialogY, dialogW, dialogH, optionStart, optionPitch, optionVisible;
    private int draggingGroup = -1, draggingOption = -1;
    private int[] dragStart;
    private boolean closing;
    private long opened = System.nanoTime(), closingAt;
    private float closeFrom = 1f;

    ControlPanel() { this(net.minecraft.client.Minecraft.getInstance().gui.screen(),false); }
    ControlPanel(Screen parent,boolean settings) {
        super(Component.literal("WanderlustClient"));
        this.parent=parent;this.settingsEntry=settings;this.uiSettings=settings;
    }
    boolean settingsEntry(){return settingsEntry;}
    private void backFromDetails(){
        if(settingsEntry){onClose();return;}
        detail=null;uiSettings=false;detailScroll=0;rebuildWidgets();
    }

    private boolean showingDetails() { return uiSettings || detail != null; }
    private MouseButtonEvent local(MouseButtonEvent event) {
        return new MouseButtonEvent(event.x() / viewScale, event.y() / viewScale, event.buttonInfo());
    }

    @Override protected void init() {
        viewScale = (float)Settings.number("ui.scale") / 100;
        if(Settings.toggle("ui.auto-fit")&&width<420)viewScale=Math.min(viewScale,(width-24f)/(3*184+2*GAP));
        filteredModules.clear();filteredRevision=-1;
        for(Feature.Group group:Feature.Group.values())MODULES.put(group,Arrays.stream(Feature.values()).filter(f->f.group==group).toList());
        positions.clear();
        collapsed.clear();
        zOrder.clear();
        for (Feature.Group group : Feature.Group.values()) {
            scrolls.putIfAbsent(group, 0);
            if (Settings.toggle(windowKey(group) + ".collapsed")) collapsed.add(group);
            zOrder.add(group);
        }
        String storedOrder = Settings.text("ui.window.order");
        if (!storedOrder.isBlank()) {
            List<Feature.Group> parsed = new ArrayList<>();
            for (String item : storedOrder.split(",")) {
                try {
                    Feature.Group group = Feature.Group.valueOf(item);
                    if (!parsed.contains(group)) parsed.add(group);
                } catch (IllegalArgumentException ignored) { }
            }
            for (Feature.Group group : Feature.Group.values()) if (!parsed.contains(group)) parsed.add(group);
            zOrder.clear();
            zOrder.addAll(parsed);
        }
        // Even with optional fitting off, keep every category inside a tiny viewport.
        for(int attempt=0;attempt<4;attempt++){
            updateWindowMetrics();
            int rows=(Feature.Group.values().length+columns-1)/columns;
            if(rows*(HEADER+rowHeight+18+GAP)<=workspaceBottom-homeTop)break;
            viewScale*=.85f;
        }
        updateWindowMetrics();
        layoutWindows();
        for (Feature.Group group : Feature.Group.values()) scroll(group, 0);

        if (!showingDetails()) {
            int navY = width < 660 ? 39 : 12;
            int navX=width<660?12:width-214;
            addButton(navX,navY,92,24,"UI Settings",PanelButton.Kind.Small,()->false,
                ()->{uiSettings=true;detailScroll=0;rebuildWidgets();});
            addButton(navX+98,navY,68,24,"Baritone",PanelButton.Kind.Small,()->false,
                ()->navigate(new BaritoneScreen(this)));
            addButton(width-36,12,24,24,"×",PanelButton.Kind.Small,()->false,this::onClose);
            int searchY=width<660?76:44;
            search=addRenderableWidget(new EditBox(font,20,searchY+3,width-252,18,PanelStyle.text("Search modules")));
            search.setBordered(false);search.setHint(PanelStyle.text(width<660?"Search…":"Search modules…  Ctrl+F"));search.setValue(query);
            search.addFormatter((value,position)->PanelStyle.text(value).getVisualOrderText());search.setValue(query);
            search.setResponder(this::updateSearch);
            clearSearch=addButton(width-224,searchY,42,22,"Clear",PanelButton.Kind.Small,()->false,()->search.setValue(""));
            clearSearch.active=!query.isBlank();
            clearSearch.setTooltip(Tooltip.create(PanelStyle.text("Clear search (Escape)")));
            addButton(width-176,searchY,76,22,"Active only",PanelButton.Kind.Small,()->activeOnly,()->{
                activeOnly=!activeOnly;filteredModules.clear();for(Feature.Group group:Feature.Group.values())scrolls.put(group,0);
            }).setTooltip(Tooltip.create(PanelStyle.text("Show only enabled modules")));
            addButton(width-94,searchY,82,22,allCollapsed()?"Expand all":"Fold all",PanelButton.Kind.Small,()->allCollapsed(),()->{
                boolean fold=!allCollapsed();for(Feature.Group group:Feature.Group.values())setCollapsed(group,fold);rebuildWidgets();
            }).setTooltip(Tooltip.create(PanelStyle.text("Collapse or expand every category")));
        } else initDetails();
    }

    private void updateWindowMetrics() {
        // Only module windows are scaled. Their coordinate space still covers the full screen.
        workspaceWidth=(int)Math.floor(width/viewScale);
        workspaceHeight=(int)Math.floor(height/viewScale);
        workspaceBottom=(int)Math.floor((height-18)/viewScale);
        columnWidth=184;
        columns=1;
        for(int candidate:new int[]{6,3,2,1})if(candidate*columnWidth+(candidate-1)*GAP+24<=workspaceWidth){columns=candidate;break;}
        rowHeight=switch(Settings.choice("ui.density")){case "Compact"->20;case "Spacious"->28;default->24;};
        homeTop=(int)Math.ceil((width<660?110:80)/viewScale);
        int groupRows=(Feature.Group.values().length+columns-1)/columns;
        int rowSpace=(workspaceBottom-homeTop)/groupRows;
        maxRows=Math.max(1,(rowSpace-HEADER-18-GAP)/rowHeight);
    }

    private static String windowKey(Feature.Group group) { return "ui.window."+group.name().toLowerCase(Locale.ROOT); }
    private boolean isCollapsed(Feature.Group group) { return collapsed.contains(group); }
    private boolean allCollapsed() { return collapsed.size()==Feature.Group.values().length; }
    private void setCollapsed(Feature.Group group,boolean fold) {
        if(fold)collapsed.add(group);else collapsed.remove(group);
        Settings.put(windowKey(group)+".collapsed",Boolean.toString(fold));
        int[] pos=positions.get(group);
        if(pos!=null){
            dragStart=pos.clone();pos[1]=clampY(group,pos[1]);
            if(!fold){
                separateWindow(group);
                if(!freePosition(group,pos[0],pos[1])){Settings.put("ui.window.layout.version","");layoutWindows();}
            }
            dragStart=null;
        }
        saveWindowPositions();
    }
    private void updateSearch(String value) {
        query=value;filteredModules.clear();if(clearSearch!=null)clearSearch.active=!query.isBlank();
        boolean changed=false;
        for(Feature.Group group:Feature.Group.values()){
            scrolls.put(group,0);
            if(!query.isBlank()&&!features(group).isEmpty()&&collapsed.remove(group)){
                Settings.put(windowKey(group)+".collapsed","false");changed=true;
            }
            int[] pos=positions.get(group);if(pos!=null)pos[1]=clampY(group,pos[1]);
        }
        if(changed)saveWindowPositions();
    }

    private void layoutWindows() {
        int startX = Math.max(4,(workspaceWidth-(columns*columnWidth+(columns-1)*GAP))/2);
        int rows = (Feature.Group.values().length + columns - 1) / columns;
        int rowSpace = rows <= 1 ? 0 : (workspaceBottom - homeTop) / rows;
        boolean restore=Settings.text("ui.window.layout.version").equals("3")
            &&Settings.text("ui.window.layout.scale").equals(Settings.format(viewScale))
            &&Settings.text("ui.window.layout.columns").equals(Integer.toString(columns))
            &&Settings.text("ui.window.layout.width").equals(Integer.toString(width))
            &&Settings.text("ui.window.layout.height").equals(Integer.toString(height));
        for (Feature.Group group : Feature.Group.values()) {
            String prefix = "ui.window." + group.name().toLowerCase(Locale.ROOT);
            int index = group.ordinal();
            int defaultX = startX + index % columns * (columnWidth + GAP);
            int defaultY = homeTop + index / columns * rowSpace;
            int x = restore?parse(Settings.text(prefix + ".x"), defaultX):defaultX;
            int y = restore?parse(Settings.text(prefix + ".y"), defaultY):defaultY;
            positions.put(group, new int[]{clampX(x), clampY(group, y)});
            if(!restore){Settings.put(prefix+".x",Integer.toString(clampX(x)));Settings.put(prefix+".y",Integer.toString(clampY(group,y)));}
        }
        Settings.put("ui.window.layout.columns",Integer.toString(columns));
        Settings.put("ui.window.layout.version","3");
        Settings.put("ui.window.layout.scale",Settings.format(viewScale));
        Settings.put("ui.window.layout.width",Integer.toString(width));
        Settings.put("ui.window.layout.height",Integer.toString(height));
    }

    private static int parse(String value, int fallback) {
        try { return value == null || value.isBlank() ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private int clampX(int x) { return Math.clamp(x, 4, Math.max(4, workspaceWidth - columnWidth - 4)); }
    private int clampY(Feature.Group group, int y) {
        return Math.clamp(y, homeTop, Math.max(homeTop, workspaceBottom - windowHeight(group) - 5));
    }

    private int windowHeight(Feature.Group group) {
        if(isCollapsed(group))return HEADER;
        int count = features(group).size();
        return HEADER + Math.min(count, maxRows) * rowHeight + (count > maxRows ? 18 : 4);
    }

    private List<Feature> features(Feature.Group group) {
        if(filteredRevision!=Settings.moduleRevision){filteredModules.clear();filteredRevision=Settings.moduleRevision;}
        return filteredModules.computeIfAbsent(group,key->{
            String filter=query.toLowerCase(Locale.ROOT);
            return MODULES.get(key).stream().filter(f->(!activeOnly||Settings.on(f.id))&&(filter.isBlank()||(f.label+" "+f.id).toLowerCase(Locale.ROOT).contains(filter))).toList();
        });
    }

    private void initDetails() {
        String owner=uiSettings?"ui":detail.id.equals("z-kill-aura")?"kill-aura":detail.id.equals("z-auto-totem")?"auto-totem":detail.id.equals("auto-trap-plus")?"auto-trap":detail.id;
        detailOptions = Settings.options.stream().filter(option -> option.owner().equals(owner)).toList();
        dialogW = Math.min(460, width - 24);
        dialogH = Math.min(Math.min(408,height-20),Math.max(200,129+Math.min(7,detailOptions.size())*39));
        dialogX = (width - dialogW) / 2;
        dialogY = (height - dialogH) / 2;
        optionStart = dialogY + 91;
        optionPitch = 39;
        optionVisible = Math.max(1, (dialogY + dialogH - 38 - optionStart) / optionPitch);
        detailScroll = Math.clamp(detailScroll, 0, Math.max(0, detailOptions.size() - optionVisible));
        shownOptions = detailOptions.subList(detailScroll, Math.min(detailOptions.size(), detailScroll + optionVisible));
        addButton(dialogX + 14, dialogY + 13, 68, 22, "‹  Back", PanelButton.Kind.Small, () -> false,
            this::backFromDetails);
        if (uiSettings) {
            int presetWidth=dialogW<380?30:44;
            for(int i=0;i<3;i++){
                int target=new int[]{60,70,85}[i];
                addButton(dialogX+88+i*(presetWidth+4),dialogY+13,presetWidth,22,Integer.toString(target)+(dialogW<380?"":"%"),PanelButton.Kind.Small,()->Math.abs(Settings.number("ui.scale")-target)<.5,
                    ()->resizeLists(target-(int)Settings.number("ui.scale"))).setTooltip(Tooltip.create(PanelStyle.text("Module size preset · "+target+"%")));
            }
            addButton(dialogX + dialogW - 106, dialogY + 13, 92, 22, "Reset layout", PanelButton.Kind.Small, () -> false, () -> {
                for (Feature.Group group : Feature.Group.values()) {
                    String key = "ui.window." + group.name().toLowerCase(Locale.ROOT);
                    Settings.put(key + ".x", ""); Settings.put(key + ".y", "");
                    Settings.put(key + ".collapsed", "false");scrolls.put(group,0);
                }
                Settings.save(); rebuildWidgets();
            });
        } else {
        if (detail == Feature.KILL_AURA || detail.id.equals("z-kill-aura")) addButton(dialogX + 90, dialogY + 13, 72, 22, "Entity list", PanelButton.Kind.Small,
            () -> false, () -> navigate(new EntitySelectionScreen(this)));
        addButton(dialogX + dialogW - 130, dialogY + 13, 116, 22,
            "Key  " + Wanderlust.binds.get(detail).getTranslatedKeyMessage().getString(), PanelButton.Kind.Small,
            () -> false, () -> navigate(new BindScreen(this, detail)));
        }
        for (int i = 0; i < shownOptions.size(); i++) {
            Settings.Option option = shownOptions.get(i);
            if (option.kind() != Settings.Kind.TEXT) continue;
            int y = optionStart + i * optionPitch + 15;
            var field = addRenderableWidget(new EditBox(font, dialogX + 18, y, dialogW - 36, 18, PanelStyle.text(option.label())));
            field.setBordered(false);
            field.addFormatter((value,position)->PanelStyle.text(value).getVisualOrderText());
            field.setMaxLength(option.key().equals("xray.blocks")?4096:512);
            field.setValue(Wanderlust.text(option.key()));
            field.setResponder(value -> Settings.setText(option, value));
        }
    }

    private int detailScroll;

    private PanelButton addButton(int x, int y, int w, int h, String label, PanelButton.Kind kind,
                                  java.util.function.BooleanSupplier selected, Runnable action) {
        PanelButton button=addRenderableWidget(new PanelButton(x, y, w, h, label, kind, selected, null, () -> {
            if (!closing) action.run();
        }));
        String hint=switch(label){
            case "UI Settings" -> "Change list size, translucency and local notifications";
            case "Accounts" -> "Manage and switch saved accounts";
            case "Baritone" -> "Control optional Baritone navigation";
            case "Reset layout" -> "Arrange and expand all categories";
            case "×" -> "Close client menu (Right Shift or Escape)";
            default -> "";
        };
        if(!hint.isBlank())button.setTooltip(Tooltip.create(PanelStyle.text(hint)));
        button.setTooltipDelay(java.time.Duration.ofMillis(300));
        return button;
    }

    private void navigate(Screen next) {
        if (closing) return;
        destination = next;
        closeFrom = openProgress(System.nanoTime());
        closing = true;
        closingAt = System.nanoTime();
    }

    @Override public void tick() {
        if (closing && (!Settings.toggle("ui.motion")||System.nanoTime() - closingAt >= CLOSE_NANOS)) {
            closing = false;
            minecraft.gui.setScreen(destination);
        }
    }

    @Override public void added() {
        closing = false;
        destination = null;
        opened = System.nanoTime();
    }

    @Override public void onClose() { navigate(parent); }

    @Override public boolean keyPressed(KeyEvent event) {
        KeyMapping menuKey = KeyMapping.get("key.wanderlust.menu");
        if (menuKey != null && menuKey.matches(event)) { MenuAccess.toggle(minecraft); return true; }
        if(closing)return true;
        if(event.key()==GLFW.GLFW_KEY_ESCAPE){
            if(showingDetails()){backFromDetails();return true;}
            if(!query.isBlank()){search.setValue("");return true;}
            if(activeOnly){activeOnly=false;filteredModules.clear();rebuildWidgets();return true;}
            onClose();return true;
        }
        if(!showingDetails()&&event.key()==GLFW.GLFW_KEY_F&&(event.modifiers()&(GLFW.GLFW_MOD_CONTROL|GLFW.GLFW_MOD_SUPER))!=0){
            setFocused(search);search.setFocused(true);search.moveCursorToEnd(false);search.setHighlightPos(0);return true;
        }
        return !closing && super.keyPressed(event);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (closing) return true;
        if (super.mouseClicked(event, doubled)) return true;
        if (showingDetails()) {
            if (inside(event.x(), event.y(), dialogX, dialogY, dialogW, dialogH)) {
                int listBottom = dialogY + dialogH - 34;
                if (event.y() >= optionStart && event.y() < listBottom) {
                    int index = detailScroll + (int)((event.y() - optionStart) / optionPitch);
                    if (index < Math.min(detailOptions.size(), detailScroll + optionVisible)) {
                    Settings.Option option = detailOptions.get(index);
                    if (option.kind() == Settings.Kind.NUMBER && event.button() == 0) {
                        draggingOption = index;
                        setSlider(option, event.x());
                        return true;
                    }
                    if (option.kind() == Settings.Kind.NUMBER && event.button() == 1) {
                        Settings.setNumber(option, Double.parseDouble(option.initial()));
                        Settings.save();
                        return true;
                    }
                    if (option.kind() == Settings.Kind.TOGGLE && event.button() == 0) {
                        Settings.setToggle(option, !Settings.toggle(option.key()));
                        Settings.save();
                        return true;
                    }
                    if (option.kind() == Settings.Kind.CHOICE && event.button() == 0) {
                        int current = option.choices().indexOf(Settings.choice(option.key()));
                        Settings.setChoice(option, option.choices().get((current + 1) % option.choices().size()));
                        Settings.save();
                        return true;
                    }
                    }
                }
                return true;
            }
            return true;
        }

        event=local(event);
        long now = System.nanoTime();
        for (int i = zOrder.size() - 1; i >= 0; i--) {
            Feature.Group group = zOrder.get(i);
            int[] pos = positions.get(group);
            float progress = windowProgress(group, now);
            double y = event.y() - slideY(progress);
            if (inside(event.x(), y, pos[0], pos[1], columnWidth, HEADER)) {
                if(event.button()==0&&(doubled||inside(event.x(),y,pos[0]+columnWidth-27,pos[1]+3,23,21))){
                    draggingGroup=-1;setCollapsed(group,!isCollapsed(group));rebuildWidgets();return true;
                }
                if (event.button() == 0) {
                    draggingGroup = group.ordinal();
                    dragStart=pos.clone();
                    zOrder.remove(group);
                    zOrder.add(group);
                    return true;
                }
                if (event.button() == 1) {
                    int index = group.ordinal();
                    int startX = Math.max(4,(workspaceWidth-(columns*columnWidth+(columns-1)*GAP))/2);
                    int rows = (Feature.Group.values().length + columns - 1) / columns;
                    int rowSpace = rows <= 1 ? 0 : (workspaceBottom - homeTop) / rows;
                    dragStart=pos.clone();
                    pos[0] = clampX(startX + index % columns * (columnWidth + GAP));
                    pos[1] = clampY(group, homeTop + index / columns * rowSpace);
                    separateWindow(group);dragStart=null;
                    saveWindowPositions();
                    return true;
                }
            }
            if(!isCollapsed(group)&&features(group).size()>maxRows&&event.button()==0&&inside(event.x(),y,pos[0],pos[1]+windowHeight(group)-18,columnWidth,18)){
                if(event.x()<pos[0]+22)scroll(group,-Math.max(1,maxRows-1));
                else if(event.x()<pos[0]+44)scroll(group,Math.max(1,maxRows-1));
                return true;
            }
            int index = rowIndex(group, event.x(), event.y(), now);
            if (index >= 0) {
                Feature feature = features(group).get(index);
                if (event.button() == 0 && event.x()<pos[0]+columnWidth-25) {
                    Settings.setEnabled(feature, !Settings.on(feature.id));
                    return true;
                }
                if (event.button() == 1 || event.button()==0) {
                    detail = feature;
                    detailScroll = 0;
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return false;
    }

    private int rowIndex(Feature.Group group, double x, double y, long now) {
        if(isCollapsed(group))return -1;
        int[] pos = positions.get(group);
        List<Feature> items = features(group);
        scroll(group,0);
        int start = scrolls.get(group), end = Math.min(items.size(), start + maxRows);
        double localY = y - slideY(windowProgress(group, now));
        for (int row = start; row < end; row++) {
            int rowY = pos[1] + HEADER + (row - start) * rowHeight + featureSlide(group, row, start, now);
            if (inside(x, localY, pos[0], rowY, columnWidth, rowHeight)) return row;
        }
        return -1;
    }

    @Override public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if(closing)return true;
        if (draggingGroup >= 0) {
            Feature.Group group = Feature.Group.values()[draggingGroup];
            int[] pos = positions.get(group);
            pos[0] = clampX(pos[0] + (int)Math.round(deltaX/viewScale));
            pos[1] = clampY(group, pos[1] + (int)Math.round(deltaY/viewScale));
            return true;
        }
        if (draggingOption >= 0 && draggingOption < detailOptions.size()) {
            setSlider(detailOptions.get(draggingOption), event.x());
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingGroup >= 0) {
            separateWindow(Feature.Group.values()[draggingGroup]);
            saveWindowPositions();
            draggingGroup = -1;
            dragStart=null;
            return true;
        }
        if (draggingOption >= 0) {
            boolean resized=detailOptions.get(draggingOption).key().equals("ui.scale");
            draggingOption = -1;
            Settings.save();
            if(resized)rebuildWidgets();
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean freePosition(Feature.Group group,int x,int y) {
        return freePositionExcept(group,x,y,null);
    }
    private boolean freePositionExcept(Feature.Group group,int x,int y,Feature.Group ignored) {
        int h=windowHeight(group);
        if(x<4||y<homeTop||x+columnWidth>workspaceWidth-4||y+h>workspaceBottom-5)return false;
        for(Feature.Group other:Feature.Group.values()) {
            if(other==group||other==ignored)continue;int[] pos=positions.get(other);
            if(pos!=null&&x<pos[0]+columnWidth+GAP&&x+columnWidth+GAP>pos[0]&&y<pos[1]+windowHeight(other)+GAP&&y+h+GAP>pos[1])return false;
        }return true;
    }
    private void separateWindow(Feature.Group group) {
        int[] pos=positions.get(group);if(freePosition(group,pos[0],pos[1]))return;
        if(dragStart!=null){
            Feature.Group swap=null;double nearest=Double.MAX_VALUE;
            for(Feature.Group other:Feature.Group.values()){
                if(other==group)continue;int[] target=positions.get(other);
                if(pos[0]<target[0]+columnWidth&&pos[0]+columnWidth>target[0]&&pos[1]<target[1]+windowHeight(other)&&pos[1]+windowHeight(group)>target[1]){
                    double distance=Math.pow(pos[0]-target[0],2)+Math.pow(pos[1]-target[1],2);
                    if(distance<nearest){nearest=distance;swap=other;}
                }
            }
            if(swap!=null){int[] target=positions.get(swap);if(freePositionExcept(group,target[0],target[1],swap)&&freePositionExcept(swap,dragStart[0],dragStart[1],group)){
                int x=target[0],y=target[1];target[0]=dragStart[0];target[1]=dragStart[1];pos[0]=x;pos[1]=y;return;
            }}
        }
        int bestX=pos[0],bestY=pos[1];double best=Double.MAX_VALUE;
        Set<Integer> xs=new TreeSet<>(List.of(4,pos[0],clampX(workspaceWidth-columnWidth-4)));
        Set<Integer> ys=new TreeSet<>(List.of(homeTop,pos[1],clampY(group,workspaceBottom-windowHeight(group)-5)));
        if(dragStart!=null){xs.add(dragStart[0]);ys.add(dragStart[1]);}
        for(Feature.Group other:Feature.Group.values())if(other!=group){int[] q=positions.get(other);xs.add(clampX(q[0]+columnWidth+GAP));xs.add(clampX(q[0]-columnWidth-GAP));ys.add(clampY(group,q[1]+windowHeight(other)+GAP));ys.add(clampY(group,q[1]-windowHeight(group)-GAP));}
        for(int y:ys)for(int x:xs){double distance=Math.pow(x-pos[0],2)+Math.pow(y-pos[1],2);if(distance<best&&freePosition(group,x,y)){best=distance;bestX=x;bestY=y;}}
        if(best==Double.MAX_VALUE&&dragStart!=null){bestX=dragStart[0];bestY=dragStart[1];}
        pos[0]=bestX;pos[1]=bestY;
    }

    private void saveWindowPositions() {
        for (Feature.Group group : Feature.Group.values()) {
            int[] pos = positions.get(group);
            String prefix = "ui.window." + group.name().toLowerCase(Locale.ROOT);
            Settings.put(prefix + ".x", Integer.toString(pos[0]));
            Settings.put(prefix + ".y", Integer.toString(pos[1]));
        }
        Settings.put("ui.window.order", String.join(",", zOrder.stream().map(Enum::name).toList()));
        Settings.save();
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (closing || vertical == 0) return false;
        if(!showingDetails()&&(com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(),GLFW.GLFW_KEY_LEFT_CONTROL)||com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(),GLFW.GLFW_KEY_RIGHT_CONTROL))){
            resizeLists(vertical>0?5:-5);return true;
        }
        if (showingDetails()) {
            if(!inside(x,y,dialogX,optionStart,dialogW,dialogH-(optionStart-dialogY)))return false;
            detailScroll = Math.clamp(detailScroll + (vertical < 0 ? 1 : -1), 0,
                Math.max(0, detailOptions.size() - optionVisible));
            rebuildWidgets();
            return true;
        }
        x/=viewScale;y/=viewScale;
        for (int i = zOrder.size() - 1; i >= 0; i--) {
            Feature.Group group = zOrder.get(i);
            int[] pos = positions.get(group);
            double localY = y - slideY(windowProgress(group, System.nanoTime()));
            if (inside(x, localY, pos[0], pos[1], columnWidth, windowHeight(group))) {
                if(!isCollapsed(group))scroll(group,vertical<0?3:-3);
                return true;
            }
        }
        return false;
    }

    private void scroll(Feature.Group group,int delta) {
        scrolls.put(group,Math.clamp(scrolls.getOrDefault(group,0)+delta,0,Math.max(0,features(group).size()-maxRows)));
    }

    private void resizeLists(int delta){
        Settings.Option option=Settings.options.stream().filter(o->o.key().equals("ui.scale")).findFirst().orElseThrow();
        Settings.setNumber(option,Settings.number("ui.scale")+delta);Settings.save();rebuildWidgets();
    }

    private void setSlider(Settings.Option option, double mouseX) {
        double left = dialogX + 20, right = dialogX + dialogW - 20;
        double ratio = Math.clamp((mouseX - left) / (right - left), 0, 1);
        double raw = option.min() + ratio * (option.max() - option.min());
        double snapped = option.min() + Math.round((raw - option.min()) / option.step()) * option.step();
        Settings.setNumber(option, snapped);
    }

    private static boolean inside(double x, double y, int left, int top, int w, int h) {
        return x >= left && x < left + w && y >= top && y < top + h;
    }

    private float openProgress(long now) {
        if(!Settings.toggle("ui.motion"))return 1;
        return PanelStyle.ease((now - opened) / (float)OPEN_NANOS);
    }

    private float windowProgress(Feature.Group group, long now) {
        if(!Settings.toggle("ui.motion"))return closing?0:1;
        float delay = group.ordinal() * 30_000_000f;
        float incoming = PanelStyle.ease((now - opened - delay) / (OPEN_NANOS - delay));
        return closing ? closeFrom * (1 - PanelStyle.ease((now - closingAt) / (float)CLOSE_NANOS)) : incoming;
    }

    private float featureProgress(Feature.Group group, int row, int start, long now) {
        if(!Settings.toggle("ui.motion"))return 1;
        if (closing) return windowProgress(group, now);
        long delay = group.ordinal() * 30_000_000L + Math.min(12,row-start) * 12_000_000L;
        return PanelStyle.ease((now - opened - delay) / (float)Math.max(1, OPEN_NANOS - delay));
    }

    private int featureSlide(Feature.Group group, int row, int start, long now) {
        return 0; // Fade rows in place so animation never overlaps neighboring rows.
    }

    private static int slideY(float progress) { return Math.round((1 - progress) * 9); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        frameSeconds=Math.min(.05f,(now-lastFrame)/1_000_000_000f);lastFrame=now;
        if(parent!=null&&minecraft.level==null)g.fillGradient(0,0,width,height,0xE0101522,0xEC080C14);
        if (Wanderlust.on("blur")) g.blurBeforeThisStratum();
        if(!showingDetails()){
            drawTopBar(g, mouseX, mouseY, now);
            g.enableScissor(0,width<660?106:76,width,height-17);
            g.pose().pushMatrix();
            g.pose().scale(viewScale,viewScale);
            for (Feature.Group group : zOrder) drawWindow(g, group, (int)(mouseX/viewScale), (int)(mouseY/viewScale), now);
            g.pose().popMatrix();
            g.disableScissor();
        }else{
            float alpha=closing?closeFrom*(1-PanelStyle.ease((now-closingAt)/(float)CLOSE_NANOS)):openProgress(now);
            for(var child:children())if(child instanceof net.minecraft.client.gui.components.AbstractWidget widget)widget.setAlpha(alpha);
            drawDetails(g,mouseX,mouseY,now);
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
        if(!showingDetails()&&!closing)drawHoverTip(g,mouseX,mouseY,now);
    }

    private void drawTopBar(GuiGraphicsExtractor g, int mouseX, int mouseY, long now) {
        float alpha = closing ? closeFrom * (1 - PanelStyle.ease((now - closingAt) / (float)CLOSE_NANOS)) : openProgress(now);
        PanelStyle.surface(g,12,8,width-24,width<660?94:62,alpha);
        PanelStyle.gem(g,29,24,11,now,alpha);
        label(g, "WANDERLUST", Settings.toggle("ui.depth")?48:22, 19, PanelStyle.TEXT, alpha);
        long enabled = Arrays.stream(Feature.values()).filter(feature -> Settings.on(feature.id)).count();
        if(width>440)label(g,enabled+" active",166,19,PanelStyle.MUTED,alpha);
        int searchY=width<660?76:44;
        PanelStyle.control(g,16,searchY,width-244,22,search!=null&&search.isFocused(),false,alpha);
        if (!showingDetails()) drawFittedLabel(g,width<660?"Drag titles · Ctrl+F search · Ctrl+wheel size":"Left-click: toggle   ·   Right-click / …: settings   ·   Drag titles   ·   + / −: fold   ·   Ctrl+F: search   ·   Ctrl+wheel: size",14,height-13,width-28,alpha,PanelStyle.TEXT);
        for (var child : children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) widget.setAlpha(alpha);
        }
    }

    private void drawWindow(GuiGraphicsExtractor g, Feature.Group group, int mouseX, int mouseY, long now) {
        int[] pos = positions.get(group);
        float progress = windowProgress(group, now);
        float alpha = !showingDetails() ? progress : progress * .26f;
        if (alpha < .015f) return;
        int slide = slideY(progress);
        int x = pos[0], y = pos[1], w = columnWidth;
        int h = windowHeight(group);
        int tint = PanelStyle.category(group);
        g.pose().pushMatrix();
        g.pose().translate(0, slide);
        PanelStyle.surface(g,x,y,w,h,alpha);
        g.fillGradient(x+1,y+1,x+w-1,y+HEADER,PanelStyle.alpha(PanelStyle.mix(0xBA202638,tint,.22f),alpha),PanelStyle.alpha(0xA11A2233,alpha));
        g.fill(x+1,y+HEADER-1,x+w-1,y+HEADER,PanelStyle.alpha(tint,alpha*.55f));
        drawFittedLabel(g,group.toString(),x+8,y+9,w-61,alpha,PanelStyle.TEXT);
        int count = features(group).size();
        String countText = Integer.toString(count);
        g.text(font, PanelStyle.text(countText), x + w - font.width(PanelStyle.text(countText)) - 33, y + 9,
            PanelStyle.alpha(tint, alpha), true);
        boolean foldHover=inside(mouseX,mouseY-slide,x+w-27,y+3,23,21);
        drawSmallControl(g,x+w-27,y+3,23,21,isCollapsed(group)?"+":"−",foldHover,alpha);
        if(isCollapsed(group)){g.pose().popMatrix();return;}
        scroll(group,0);
        int start = scrolls.get(group), end = Math.min(count, start + maxRows);
        for (int row = start; row < end; row++) {
            Feature feature = features(group).get(row);
            int rowY = y + HEADER + (row - start) * rowHeight + featureSlide(group, row, start, now);
            float rowAlpha = alpha * featureProgress(group, row, start, now);
            boolean hover = !showingDetails() && inside(mouseX, mouseY - slide, x, rowY, w, rowHeight);
            if (rowAlpha < .015f) continue;
            if ((row & 1) == 0) g.fill(x + 1, rowY, x + w - 1, rowY + rowHeight, PanelStyle.alpha(0x10000000, rowAlpha));
            float over=Settings.toggle("ui.motion")?PanelStyle.approach(rowHover.getOrDefault(feature,0f),hover?1:0,frameSeconds,18):(hover?1:0);
            rowHover.put(feature,over);
            if(over>.01f)g.fill(x+1,rowY,x+w-1,rowY+rowHeight,PanelStyle.alpha(tint,rowAlpha*over*.16f));
            if (Settings.on(feature.id)) {
                g.fill(x + 1, rowY, x + w - 1, rowY + rowHeight, PanelStyle.alpha(tint, rowAlpha*.13f));
                g.fill(x + 1, rowY + 3, x + 3, rowY + rowHeight - 3, PanelStyle.alpha(tint, rowAlpha));
            }
            int checkY=rowY+(rowHeight-9)/2;
            g.fill(x+7,checkY,x+16,checkY+9,PanelStyle.alpha(Settings.on(feature.id)?tint:0xFFAAB5C9,rowAlpha));
            if(!Settings.on(feature.id))g.fill(x+8,checkY+1,x+15,checkY+8,PanelStyle.alpha(0xFF1B2130,rowAlpha));
            drawSmallControl(g,x+w-24,rowY+2,19,rowHeight-4,"…",hover&&mouseX>=x+w-25,rowAlpha);
            drawFittedLabel(g, feature.label, x + 22, rowY + (rowHeight - 9) / 2, w - 49, rowAlpha,
                Settings.on(feature.id) ? PanelStyle.TEXT : 0xFFE0E4EF);
        }
        if (count > maxRows) {
            String page = (start + 1) + "–" + end + " / " + count;
            drawSmallControl(g,x+3,y+h-17,18,14,"↑",inside(mouseX,mouseY-slide,x+3,y+h-17,18,14),alpha);
            drawSmallControl(g,x+25,y+h-17,18,14,"↓",inside(mouseX,mouseY-slide,x+25,y+h-17,18,14),alpha);
            drawFittedLabel(g,page,x+48,y+h-13,w-53,alpha,PanelStyle.TEXT);
            int trackY=y+HEADER,trackH=Math.min(count,maxRows)*rowHeight;
            int thumbH=Math.max(8,trackH*maxRows/count);
            int thumbY=trackY+(trackH-thumbH)*start/Math.max(1,count-maxRows);
            g.fill(x+w-2,trackY,x+w,trackY+trackH,PanelStyle.alpha(0x553E485E,alpha));
            g.fill(x+w-2,thumbY,x+w,thumbY+thumbH,PanelStyle.alpha(PanelStyle.accentLight(),alpha));
        }
        g.pose().popMatrix();
    }

    private void drawSmallControl(GuiGraphicsExtractor g,int x,int y,int w,int h,String text,boolean hover,float alpha){
        if(!text.equals("…")||hover)PanelStyle.control(g,x,y,w,h,hover,false,alpha);
        label(g,text,x+(w-font.width(PanelStyle.text(text)))/2,y+(h-9)/2,PanelStyle.TEXT,alpha);
    }

    private void drawHoverTip(GuiGraphicsExtractor g,int mouseX,int mouseY,long now){
        String hint="";double x=mouseX/viewScale,y=mouseY/viewScale;
        for(int i=zOrder.size()-1;i>=0;i--){
            Feature.Group group=zOrder.get(i);int[] pos=positions.get(group);
            double localY=y-slideY(windowProgress(group,now));
            if(inside(x,localY,pos[0],pos[1],columnWidth,HEADER)){
                hint=x>=pos[0]+columnWidth-27?(isCollapsed(group)?"Expand ":"Collapse ")+group:
                    group+" · "+features(group).size()+" modules\nDrag to move · Double-click to fold\nRight-click to reset position";
                break;
            }
            int row=rowIndex(group,x,y,now);
            if(row>=0){
                Feature feature=features(group).get(row);
                hint=feature.label+(Settings.on(feature.id)?" · ON":" · OFF")+"\n"+feature.description+"\nLeft-click: toggle · Right-click / …: settings\nKey: "+Wanderlust.binds.get(feature).getTranslatedKeyMessage().getString();
                break;
            }
        }
        if(!hint.equals(hoverHint)){hoverHint=hint;hoverSince=now;}
        if(!hint.isBlank()&&now-hoverSince>400_000_000L){
            List<net.minecraft.util.FormattedCharSequence> lines=new ArrayList<>();
            for(String line:hint.split("\n"))lines.addAll(font.split(PanelStyle.text(line),Math.min(320,width-32)));
            int tw=0;for(var line:lines)tw=Math.max(tw,font.width(line));
            int th=lines.size()*13+16,tx=Math.clamp(mouseX+12,8,Math.max(8,width-tw-24)),ty=Math.clamp(mouseY+16,8,Math.max(8,height-th-8));
            PanelStyle.control(g,tx,ty,tw+16,th,false,false,1);
            for(int i=0;i<lines.size();i++)g.text(font,lines.get(i),tx+8,ty+8+i*13,i==0?PanelStyle.TEXT:PanelStyle.MUTED,false);
        }
    }

    private void drawDetails(GuiGraphicsExtractor g, int mouseX, int mouseY, long now) {
        long age = now - opened;
        float alpha = closing ? closeFrom * (1 - PanelStyle.ease((now - closingAt) / (float)CLOSE_NANOS)) : openProgress(now);
        if (alpha < .015f) return;
        PanelStyle.surface(g,dialogX,dialogY,dialogW,dialogH,alpha);
        label(g, uiSettings ? "UI Settings" : detail.label, dialogX + 18, dialogY + 45, PanelStyle.TEXT, alpha);
        drawFittedLabel(g, uiSettings ? "Fine-tune size, spacing and motion. Controls stay at native size." : detail.description, dialogX + 18, dialogY + 62, dialogW - 36, alpha * .9f, PanelStyle.TEXT);
        for (int i = 0; i < shownOptions.size(); i++) {
            Settings.Option option = shownOptions.get(i);
            int y = optionStart + i * optionPitch;
            if ((i & 1) == 0) PanelStyle.round(g, dialogX + 10, y, dialogW - 20, optionPitch - 1, 4,
                PanelStyle.alpha(0x14000000, alpha));
            drawOption(g, option, y, alpha);
        }
        if (detailOptions.isEmpty()) label(g, "No additional settings for this module.", dialogX + 18, optionStart + 6,
            PanelStyle.MUTED, alpha);
        String footer = detailOptions.size() > optionVisible ? "Scroll for more  ·  Right-click a slider to reset" : "Settings save when you close the panel";
        drawFittedLabel(g,footer,dialogX+18,dialogY+dialogH-22,dialogW-(detailOptions.size()>optionVisible?110:36),alpha*.85f,PanelStyle.MUTED);
        if (detailOptions.size() > optionVisible) {
            String page = (detailScroll + 1) + "–" + Math.min(detailOptions.size(), detailScroll + optionVisible) + " / " + detailOptions.size();
            label(g, page, dialogX + dialogW - font.width(PanelStyle.text(page)) - 18, dialogY + dialogH - 22, PanelStyle.MUTED, alpha);
        }
    }

    private void drawOption(GuiGraphicsExtractor g, Settings.Option option, int y, float alpha) {
        int labelWidth = option.kind() == Settings.Kind.CHOICE ? dialogW - 180 : option.kind() == Settings.Kind.TOGGLE ? dialogW - 88 : dialogW - 90;
        drawFittedLabel(g, option.label(), dialogX + 18, y + 5, labelWidth, alpha, PanelStyle.TEXT);
        switch (option.kind()) {
            case NUMBER -> {
                String value = Wanderlust.text(option.key());
                var comp = PanelStyle.text(value);
                g.text(font, comp, dialogX + dialogW - font.width(comp) - 20, y + 5, PanelStyle.alpha(PanelStyle.accentLight(), alpha), true);
                int sx = dialogX + 20, sy = y + 25, sw = dialogW - 40;
                PanelStyle.round(g, sx, sy, sw, 3, 2, PanelStyle.alpha(0xFF343B49, alpha));
                double ratio = (Wanderlust.number(option.key()) - option.min()) / (option.max() - option.min());
                int fill = (int)Math.round(sw * Math.clamp(ratio, 0, 1));
                PanelStyle.round(g, sx, sy, fill, 3, 2, PanelStyle.alpha(PanelStyle.accent(), alpha));
                PanelStyle.round(g, sx + fill - 4, sy - 3, 8, 9, 4, PanelStyle.alpha(PanelStyle.accentLight(), alpha));
            }
            case TOGGLE -> {
                boolean active = Settings.toggle(option.key());
                int sx = dialogX + dialogW - 48, sy = y + 5;
                PanelStyle.round(g, sx, sy, 30, 15, 8, PanelStyle.alpha(active ? PanelStyle.accent() : 0xFF3B4557, alpha));
                PanelStyle.round(g, sx + (active ? 17 : 2), sy + 2, 11, 11, 6, PanelStyle.alpha(0xFFF0ECF8, alpha));
            }
            case CHOICE -> {
                String value = Settings.choice(option.key());
                var comp = PanelStyle.text("‹  " + value + "  ›");
                int cw = Math.max(104, font.width(comp) + 18);
                PanelStyle.control(g, dialogX + dialogW - cw - 18, y + 3, cw, 20, false, false, alpha);
                g.text(font, comp, dialogX + dialogW - cw - 9, y + 8, PanelStyle.alpha(PanelStyle.accentLight(), alpha), true);
            }
            case TEXT -> {
                PanelStyle.round(g, dialogX + 14, y + 15, dialogW - 28, 21, 4, PanelStyle.alpha(0x57242B38, alpha));
            }
        }
    }

    private void drawFittedLabel(GuiGraphicsExtractor g, String value, int x, int y, int available, float alpha, int color) {
        if(available<=0)return;
        String shown = value;
        if(font.width(PanelStyle.text(shown))>available){
            int end=shown.length();
            while(end>0&&font.width(PanelStyle.text(shown.substring(0,end)+"…"))>available)end--;
            shown=end==0?"":shown.substring(0,end)+"…";
        }
        label(g, shown, x, y, color, alpha);
    }

    private void label(GuiGraphicsExtractor g, String value, int x, int y, int color, float alpha) {
        if (alpha < .015f) return;
        g.text(font, PanelStyle.text(value), x, y, PanelStyle.alpha(color, alpha), minecraft.level!=null);
    }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) { }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void removed() { Settings.save(); }
}
