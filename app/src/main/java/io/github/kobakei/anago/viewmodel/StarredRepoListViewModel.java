package io.github.kobakei.anago.viewmodel;

import android.app.Activity;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.util.Pair;

import javax.inject.Inject;

import io.github.kobakei.anago.activity.RepoActivity;
import io.github.kobakei.anago.entity.Repo;
import io.github.kobakei.anago.fragment.BaseFragment;
import io.github.kobakei.anago.usecase.GetStarredReposUseCase;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * パブリックリポジトリ一覧画面ビューモデル
 * Created by keisuke on 2016/09/18.
 */

public class StarredRepoListViewModel extends FragmentViewModel {

    private final GetStarredReposUseCase getStarredReposUseCase;

    public ObservableArrayList<Pair<Repo, Boolean>> repos;
    public ObservableBoolean isConnecting;
    public ObservableBoolean isRefreshing;

    @Inject
    public StarredRepoListViewModel(BaseFragment fragment, GetStarredReposUseCase getStarredReposUseCase) {
        super(fragment);

        this.getStarredReposUseCase = getStarredReposUseCase;

        repos = new ObservableArrayList<>();
        isConnecting = new ObservableBoolean(true);
        isRefreshing = new ObservableBoolean(false);

        refreshData();
    }

    public void onRefresh() {
        Timber.v("onRefresh");
        this.isRefreshing.set(true);
        refreshData();
    }

    public void onItemClick(int position) {
        Repo repo = repos.get(position).first;
        RepoActivity.startActivity(getFragment().getActivity(), repo.owner.login, repo.name);
    }

    private void refreshData() {
        getStarredReposUseCase.run()
                .compose(getFragment().bindToLifecycle().forSingle())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(repos1 -> {
                    this.isRefreshing.set(false);
                    repos.clear();
                    repos.addAll(repos1);
                    isConnecting.set(false);
                });
    }
}
