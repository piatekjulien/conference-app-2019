package io.github.droidkaigi.confsched2019.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import io.github.droidkaigi.confsched2019.ext.android.changedNonNull
import io.github.droidkaigi.confsched2019.model.Session
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.databinding.FragmentSessionDetailBinding
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SessionActionCreator
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SessionDetailActionCreator
import io.github.droidkaigi.confsched2019.session.ui.store.SessionDetailStore
import io.github.droidkaigi.confsched2019.session.ui.store.SessionStore
import me.tatarka.injectedvmprovider.ktx.injectedViewModelProvider
import javax.inject.Inject
import javax.inject.Named

class SessionDetailFragment : Fragment(), HasSupportFragmentInjector {

    @Inject lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var sessionActionCreator: SessionActionCreator
    @Inject lateinit var sessionDetailActionCreator: SessionDetailActionCreator

    lateinit var binding: FragmentSessionDetailBinding

    @Inject lateinit var sessionStore: SessionStore

    @Inject lateinit var sessionDetailStoreFactory: SessionDetailStore.Factory

    private val sessionDetailStore: SessionDetailStore by lazy {
        injectedViewModelProvider
            .get(SessionDetailStore::class.java.name) {
                sessionDetailStoreFactory.create(sessionDetailFragmentArgs.session)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_session_detail, container,
            false)
        return binding.root
    }

    private lateinit var sessionDetailFragmentArgs: SessionDetailFragmentArgs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        sessionDetailFragmentArgs = SessionDetailFragmentArgs.fromBundle(arguments)
        binding.favorite.setOnClickListener {
            val session = sessionDetailStore.session.value ?: return@setOnClickListener
            sessionActionCreator.toggleFavorite(session)
        }
        sessionDetailActionCreator.load(sessionDetailFragmentArgs.session)
        sessionDetailStore.session.changedNonNull(
            this) { session: Session.SpeechSession ->
            binding.session = session
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return childFragmentInjector
    }
}

@AssistedModule
@Module(includes = [AssistedInject_SessionDetailFragmentModule::class])
abstract class SessionDetailFragmentModule {

    @Module
    companion object {
        @JvmStatic @Provides
        @Named("SessionDetailFragment")
        fun providesLifecycle(sessionsFragment: SessionDetailFragment): LifecycleOwner {
            return sessionsFragment.viewLifecycleOwner
        }

        @JvmStatic @Provides
        fun provideActivity(sessionsFragment: SessionDetailFragment): FragmentActivity {
            return sessionsFragment.requireActivity()
        }
    }
}