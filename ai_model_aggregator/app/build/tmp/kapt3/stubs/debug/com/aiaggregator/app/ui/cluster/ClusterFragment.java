package com.aiaggregator.app.ui.cluster;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000p\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\u0018\u00002\u00020\u0001:\u000256B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002J$\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010&2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\u001a\u0010)\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\"2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\u0010\u0010+\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\"H\u0002J\u0010\u0010,\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\"H\u0002J\b\u0010-\u001a\u00020\u001eH\u0002J\b\u0010.\u001a\u00020\u001eH\u0002J\u0016\u0010/\u001a\u00020\u001e2\f\u00100\u001a\b\u0012\u0004\u0012\u00020 0\u0019H\u0002J\u0010\u00101\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002J\u001e\u00102\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\"2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019H\u0002J\u0010\u00104\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\"H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u001b\u0010\u000b\u001a\u00020\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\n\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0018\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00170\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/aiaggregator/app/ui/cluster/ClusterFragment;", "Landroidx/fragment/app/Fragment;", "()V", "apiService", "Lcom/aiaggregator/app/data/ApiService;", "clusterRecordDao", "Lcom/aiaggregator/app/data/ClusterRecordDao;", "getClusterRecordDao", "()Lcom/aiaggregator/app/data/ClusterRecordDao;", "clusterRecordDao$delegate", "Lkotlin/Lazy;", "db", "Lcom/aiaggregator/app/data/AppDatabase;", "getDb", "()Lcom/aiaggregator/app/data/AppDatabase;", "db$delegate", "gson", "Lcom/google/gson/Gson;", "handler", "Landroid/os/Handler;", "isRunning", "", "lastPrompt", "", "lastResults", "", "Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterResult;", "selectedModels", "", "deleteClusterRecord", "", "record", "Lcom/aiaggregator/app/data/ClusterRecordEntity;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "view", "resetCluster", "runCluster", "saveClusterRecord", "showClusterHistory", "showClusterHistoryDialog", "records", "showRecordDetail", "showResults", "results", "updateStatus", "ClusterModelAdapter", "ClusterResult", "app_debug"})
public final class ClusterFragment extends androidx.fragment.app.Fragment {
    private com.aiaggregator.app.data.ApiService apiService;
    @org.jetbrains.annotations.NotNull
    private final java.util.Set<java.lang.String> selectedModels = null;
    @org.jetbrains.annotations.NotNull
    private final android.os.Handler handler = null;
    private boolean isRunning = false;
    @org.jetbrains.annotations.NotNull
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy clusterRecordDao$delegate = null;
    @org.jetbrains.annotations.NotNull
    private java.lang.String lastPrompt = "";
    @org.jetbrains.annotations.Nullable
    private java.util.List<com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterResult> lastResults;
    
    public ClusterFragment() {
        super();
    }
    
    private final com.aiaggregator.app.data.AppDatabase getDb() {
        return null;
    }
    
    private final com.aiaggregator.app.data.ClusterRecordDao getClusterRecordDao() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override
    public void onViewCreated(@org.jetbrains.annotations.NotNull
    android.view.View view, @org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void updateStatus(android.view.View view) {
    }
    
    private final void runCluster(android.view.View view) {
    }
    
    private final void saveClusterRecord() {
    }
    
    private final void showClusterHistory() {
    }
    
    private final void showClusterHistoryDialog(java.util.List<com.aiaggregator.app.data.ClusterRecordEntity> records) {
    }
    
    private final void showRecordDetail(com.aiaggregator.app.data.ClusterRecordEntity record) {
    }
    
    private final void deleteClusterRecord(com.aiaggregator.app.data.ClusterRecordEntity record) {
    }
    
    private final void showResults(android.view.View view, java.util.List<com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterResult> results) {
    }
    
    private final void resetCluster(android.view.View view) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u0010\u0012\f\u0012\n0\u0002R\u00060\u0000R\u00020\u00030\u0001:\u0001\u0015B\u001f\u0012\u0018\u0010\u0004\u001a\u0014\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0005\u00a2\u0006\u0002\u0010\tJ\b\u0010\f\u001a\u00020\rH\u0016J \u0010\u000e\u001a\u00020\b2\u000e\u0010\u000f\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u0010\u001a\u00020\rH\u0016J \u0010\u0011\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\rH\u0016R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0004\u001a\u0014\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterModelAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterModelAdapter$ViewHolder;", "Lcom/aiaggregator/app/ui/cluster/ClusterFragment;", "onCheckChanged", "Lkotlin/Function2;", "Lcom/aiaggregator/app/models/AiModel;", "", "", "(Lcom/aiaggregator/app/ui/cluster/ClusterFragment;Lkotlin/jvm/functions/Function2;)V", "allModels", "", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ViewHolder", "app_debug"})
    public final class ClusterModelAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterModelAdapter.ViewHolder> {
        @org.jetbrains.annotations.NotNull
        private final kotlin.jvm.functions.Function2<com.aiaggregator.app.models.AiModel, java.lang.Boolean, kotlin.Unit> onCheckChanged = null;
        @org.jetbrains.annotations.NotNull
        private final java.util.List<com.aiaggregator.app.models.AiModel> allModels = null;
        
        public ClusterModelAdapter(@org.jetbrains.annotations.NotNull
        kotlin.jvm.functions.Function2<? super com.aiaggregator.app.models.AiModel, ? super java.lang.Boolean, kotlin.Unit> onCheckChanged) {
            super();
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterModelAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull
        com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterModelAdapter.ViewHolder holder, int position) {
        }
        
        @java.lang.Override
        public int getItemCount() {
            return 0;
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterModelAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterModelAdapter;Landroid/view/View;)V", "checkbox", "Landroid/widget/CheckBox;", "nameText", "Landroid/widget/TextView;", "tagsText", "vendorText", "bind", "", "model", "Lcom/aiaggregator/app/models/AiModel;", "app_debug"})
        public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull
            private final android.widget.CheckBox checkbox = null;
            @org.jetbrains.annotations.NotNull
            private final android.widget.TextView nameText = null;
            @org.jetbrains.annotations.NotNull
            private final android.widget.TextView vendorText = null;
            @org.jetbrains.annotations.NotNull
            private final android.widget.TextView tagsText = null;
            
            public ViewHolder(@org.jetbrains.annotations.NotNull
            android.view.View itemView) {
                super(null);
            }
            
            public final void bind(@org.jetbrains.annotations.NotNull
            com.aiaggregator.app.models.AiModel model) {
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\tH\u00c6\u0003J1\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\t2\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001c"}, d2 = {"Lcom/aiaggregator/app/ui/cluster/ClusterFragment$ClusterResult;", "", "model", "Lcom/aiaggregator/app/models/AiModel;", "content", "", "responseTimeMs", "", "isError", "", "(Lcom/aiaggregator/app/models/AiModel;Ljava/lang/String;JZ)V", "getContent", "()Ljava/lang/String;", "()Z", "getModel", "()Lcom/aiaggregator/app/models/AiModel;", "getResponseTimeMs", "()J", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    static final class ClusterResult {
        @org.jetbrains.annotations.NotNull
        private final com.aiaggregator.app.models.AiModel model = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String content = null;
        private final long responseTimeMs = 0L;
        private final boolean isError = false;
        
        public ClusterResult(@org.jetbrains.annotations.NotNull
        com.aiaggregator.app.models.AiModel model, @org.jetbrains.annotations.NotNull
        java.lang.String content, long responseTimeMs, boolean isError) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.aiaggregator.app.models.AiModel getModel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getContent() {
            return null;
        }
        
        public final long getResponseTimeMs() {
            return 0L;
        }
        
        public final boolean isError() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.aiaggregator.app.models.AiModel component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        public final long component3() {
            return 0L;
        }
        
        public final boolean component4() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.aiaggregator.app.ui.cluster.ClusterFragment.ClusterResult copy(@org.jetbrains.annotations.NotNull
        com.aiaggregator.app.models.AiModel model, @org.jetbrains.annotations.NotNull
        java.lang.String content, long responseTimeMs, boolean isError) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
}