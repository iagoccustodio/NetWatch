package br.com.netwatch;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NetWatchApp extends Application {
    private final Store store = new Store();
    private final TableView<Store.SwitchRow> table = new TableView<>();
    private final Label summary = new Label();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override public void start(Stage stage) {
        store.initialize();
        stage.setTitle("NetWatch | Monitoramento de switches");
        Label brand = new Label("NETWATCH  /  CENTRAL DE OPERAÇÕES"); brand.getStyleClass().add("brand");
        Label mode = new Label("● MODO DEMONSTRAÇÃO — sem conexões externas"); mode.getStyleClass().add("mode");
        HBox header = new HBox(24, brand, mode); header.setPadding(new Insets(20)); header.getStyleClass().add("header");
        header.setHgrow(mode, Priority.ALWAYS);
        buildTable();
        summary.getStyleClass().add("summary");
        Button add = new Button("＋ Cadastrar switch"); add.setOnAction(e -> addSwitch());
        Button flip = new Button("Simular UP / DOWN"); flip.setOnAction(e -> flipSelected());
        Button logs = new Button("CLEAR LOG"); logs.setOnAction(e -> clearLogs());
        Button export = new Button("Exportar TXT"); export.setOnAction(e -> export(stage));
        Button refresh = new Button("Atualizar"); refresh.setOnAction(e -> refresh());
        HBox actions = new HBox(10, add, flip, logs, export, refresh); actions.setPadding(new Insets(12, 0, 12, 0));
        PieChart chart = new PieChart(); chart.setTitle("Disponibilidade atual"); chart.setLegendVisible(true); chart.setPrefSize(300, 240);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox content = new VBox(12, summary, actions, table, chart); content.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);
        BorderPane root = new BorderPane(content, header, null, null, null);
        refresh();
        Scene scene = new Scene(root, 1120, 760); scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/netwatch.css")).toExternalForm());
        stage.setMinWidth(900); stage.setMinHeight(650); stage.setScene(scene); stage.show();
        chart.dataProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> {
            long up=store.count("UP"), down=store.count("DOWN"), unknown=store.count("UNKNOWN");
            return FXCollections.observableArrayList(new PieChart.Data("UP · "+up,up),new PieChart.Data("DOWN · "+down,down),new PieChart.Data("Desconhecido · "+unknown,unknown));
        }, table.getItems()));
    }
    private void buildTable() {
        addColumn("Nome", Store.SwitchRow::name); addColumn("Fabricante / modelo", Store.SwitchRow::model);
        addColumn("Endereço", Store.SwitchRow::address); addColumn("Localização", Store.SwitchRow::location);
        addColumn("Estado", r -> r.status()); addColumn("Última atualização", r -> r.checkedAt());
    }
    private void addColumn(String title, java.util.function.Function<Store.SwitchRow,String> value) {
        TableColumn<Store.SwitchRow,String> col = new TableColumn<>(title); col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(value.apply(c.getValue()))); table.getColumns().add(col);
    }
    private void refresh() {
        table.setItems(FXCollections.observableArrayList(store.switches()));
        summary.setText("SWITCHES  " + store.switches().size() + "     UP  " + store.count("UP") + "     DOWN  " + store.count("DOWN") + "     DESCONHECIDO  " + store.count("UNKNOWN") + "     EVENTOS  " + store.eventCount());
    }
    private void addSwitch() {
        Dialog<List<String>> d = new Dialog<>(); d.setTitle("Cadastrar switch fictício"); d.setHeaderText("Adicione um equipamento ao inventário local."); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        TextField name=new TextField(), vendor=new TextField("Demo"), address=new TextField(), location=new TextField("Laboratório"); name.setPromptText("Nome"); address.setPromptText("IP ou hostname fictício");
        GridPane g=new GridPane(); g.setHgap(10);g.setVgap(10);g.addRow(0,new Label("Nome"),name);g.addRow(1,new Label("Fabricante/modelo"),vendor);g.addRow(2,new Label("Endereço"),address);g.addRow(3,new Label("Localização"),location);d.getDialogPane().setContent(g);
        d.setResultConverter(b -> b==ButtonType.OK?List.of(name.getText(),vendor.getText(),address.getText(),location.getText()):null);
        d.showAndWait().ifPresent(v -> { if(v.get(0).isBlank()||v.get(2).isBlank()){alert("Nome e endereço são obrigatórios.");return;} store.insert(v);refresh(); });
    }
    private void flipSelected() { Store.SwitchRow r=table.getSelectionModel().getSelectedItem(); if(r==null){alert("Selecione um switch.");return;} store.transition(r.id(),r.status().equals("UP")?"DOWN":"UP");refresh(); }
    private void clearLogs() { Alert a=new Alert(Alert.AlertType.CONFIRMATION,"Apagar eventos locais? Uma entrada será registrada na auditoria, que será preservada.",ButtonType.OK,ButtonType.CANCEL);a.setHeaderText("CLEAR LOG");if(a.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK){store.clearLogs();refresh();} }
    private void export(Stage stage) {
        DirectoryChooser chooser=new DirectoryChooser();chooser.setTitle("Escolha a pasta do relatório");var dir=chooser.showDialog(stage);if(dir==null)return;
        String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")); Path target=Path.of(dir.toString(),"netwatch_relatorio_up_down_"+stamp+".txt");int suffix=1;while(Files.exists(target))target=Path.of(dir.toString(),"netwatch_relatorio_up_down_"+stamp+"_"+(suffix++)+".txt");
        try {Files.writeString(target,store.report(fmt),StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);new Alert(Alert.AlertType.INFORMATION,"Relatório salvo em:\n"+target).showAndWait();}catch(IOException ex){alert("Não foi possível gravar o relatório: "+ex.getMessage());}
    }
    private void alert(String msg){new Alert(Alert.AlertType.WARNING,msg).showAndWait();}
    @Override public void stop(){store.close();}
    public static void main(String[] args){launch(args);}
}
