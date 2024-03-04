/*
 * Copyright 2016-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.uml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.ObjectUtils;

public class UmlStateMachineModelFactoryTests extends AbstractUmlTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSimpleFlat1() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.registerAction("action1", new LatchAction());
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleFlat2() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		DefaultStateMachineComponentResolver<String, String> resolver = new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("action1", new LatchAction());
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setStateMachineComponentResolver(resolver);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleSubmachine() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals("S11")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isEqualTo("S1");
			} else if (stateData.getState().equals("S12")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isEqualTo("S1");
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleRootRegions() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Test {@link StateMachine} vs {@link StateMachineModel} consistency.<BR/>
	 * In this (failing) test, one can notice that the statemachine instance has a duplicated transition "S3->S4" as illustrated here<BR/>
	 * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAKQAAAFMCAIAAADKt4BNAAAAKnRFWHRjb3B5bGVmdABHZW5lcmF0ZWQgYnkgaHR0cHM6Ly9wbGFudHVtbC5jb212zsofAAABomlUWHRwbGFudHVtbAABAAAAeJzlk89O20AQxu/zFF/oAajkxHFMVfnQRIBBDQlBONADRdVib5KV7F1r/wShqu/e3aRAUFFfoHuanfnmN6uZ2ZGxTFvX1LQvleXY63Q6uJzN8W12ffH18hz+/l0GZ6610hASRauFXKKwzPKGlSshOTwALdOGawyHwz0wEyA//kCI9lfWtibr9dqayVCuW6qmZwIiqgRbatYEkag4Km5KLVorlATTnGHhy26UeBR2pZzdldAmhzetfXrjpruP94juPsSbcx99gXR1TVtOMIGfBPylKvreu1XtmgmRvxR9RJV6lG8SEmTIg7QYvBNNQzR5t9LgFb9jpttK/2D98goacVmFqdHoZYD03/fiKizXzXSCNdcm7E+/m8RJ2h0czFcOZ/wB/SMkcZbGWXyEk3yOED6kg/OrCYxyuvTrJ4zV4sGFzh3SmK0Zrp20ouEZZi2X49OLZwdyuRZayYZLS+Pb6avgUxodC4uCa/8S3E7plC+Yq63PKFXlf0+Gm/lZ9JkmTC4dW3r2QtOJ8lz9lOE4p99gSQU0mbwaIAAAHepJREFUeF7tnXtcFVUewMnwlYrP0HwgYGS0q+6urpprPmp908e2Pmu1Wm674qZZbvoRjJchKCrxsQRWQxTTfO2aJGES0WprAiWbn41VSAVWQHkoD8FHiMH+Yj5Nl99v7mWYO3PvzJnz/YPP3N85Z+75ne+dmXPm3st1aeaYBhcc4LALl20iuGwTwWWbCC7bRJhIdmFhYWxs7DPPPDNhwgRvb2/4C9sQgTiuyiimkF1dXb169WofHx9vKSAOpVAHN2MO9mXn5+dPmjQJGyZAHaiJG7MF47JLS0vHjBmDxVoBakJ9vAuGYFn23bt3/fz8sFKbQH1ohXfECizL/vvf/45lygBa4R2xAsuyp0+fjk3KAFrhHbECs7KLioqwRtlAW7w7JmBWdkpKCnYoG2iLd8cEzMpOTEzEDmUDbfHumIDLloDLNhj8NE5hVjafoFGYld3Ml14ElmXzmyoIlmXz26UIlmU38zdCWsO47Gb+FqcF7Mtu5h9e+BFTyBbgH0sykWxLXFzMmLgZc27msk0Fl20iuGwTwWWbCC7bRHDZJoLLNhFctongsk0El20iuGwTwWWbCC7bRHDZJoLLNhFctongsk0El20iuGwTwWWbCC7bRHDZJoLLNhFctongsk0El20iuGwTwWWbCC7bRHDZJoLLNhFctongsk0El20iuGwTwWWbCC7bRHDZJoLLNhFctongsk0El20iuGwTwWWbCC7bRHDZJoLLZpm8vDzLh0g2KmUVs8iOiYk5ceKE+NBSNsShVHzIMGaRXVlZ2a9fP9G3KBsiEIfSn6qyi1lkA/PmzevSpYvgW5AN2xCBOK7KKCaSfe7cOXDcrVs3cAwb8Be2YQPiuCqjmEg2MGbMGFdX1169eoFj+AvbEMGV2MVcstPT0zt37gyn7u7du8Nf2IYIrsQu5pINDBo0yOVHYBsXM43pZG/btq1Tp05gGv7CNi5mGtPJ/u677+AcDrLhL2zjYqYxnWxg1apVIBv+4gLWMaPsysrKHj16mORGiiXtkN3U1FRSUvLee++tMz6LFy/GIQOyY8eOgoIC8IJVWUGWbNjdiRMngoODY2Njc3Nzr3P0QX5+fmJiYmho6L59++7cuYO1EdqWXV1dDZoPHTqEn4qjG7KzswMDAzMzM7G81rQh++LFizCRKS4uxrvn6A84yrdu3WrjrG5LdlVVFZiGv3ivHL2SkZERFxdnzbdV2dAgJCSEH9OG48iRIwcPHsQ6W7AqG2Zk/DptUCIjIyUXltKy4bCGSVltbS3eDccIlJaWwsIMS7UmG9bT8fHxeB8c4xAVFXXz5k2kVVr2rl27+Hra0Jw6dSolJQVplZYNJwHc2gS4uLigDeNSX1+/fv16pJXL/gmWZAPmkg3O3nnnHQ8Pjw4dOnTs2HHv3r1iHFWztmFo6ByNcdkTJ07897//DcuK999/H3yLcVTN2oahMZ3s7Oxsy4dow1qcyzYe4Ozq1auWD9GGtTiXbTzalApUVVXROJdtPOTIhis6jXPZxsOabJipwXwNZm1gesqUKdQxl208rMkWZuawHnvwwQf37dtHHXPZHIPBZZsILttEcNkmgss2EVy2ieCyTQSXbSLkyl6wYME6jsEBiUirtOy33noLhzhGg0rkspmFSuSymYVK5LKZhUrkspmFSuSymYVK5LKZhUrkspmFSuSymYVK5LKZhUpkXPbNmzdPnz59tAXYoN9iZRgqkVnZp06deumllx5++GFvC+AhBKEI12YRKpFB2bW1tf7+/paOKVABquGWbEElsib78uXLTzzxBHYrBVSDyrg9Q1CJTMm+deuWn58ftmodqAxN8F5YgUpkSnZUVBT22RbQBO+FFahEdmSXlZX5+vpimW0BTaAh3hcTUInsyN6+fTs2KQ9oiPfFBFQiO7Kff/55rFEe0BDviwmoRHZk/+Y3v8Ea5QEN8b6YgEpkRza6fyIfaIj3xQRUIjuyx44dizXKAxrifTEBlciO7Llz52KN8oCGeF9MQCWyIzs6OhprlAc0xPtiAiqRHdlnz54dNmwYNtkW0AQa4n0xAZXIjmxgyZIlWGZbQBO8F1agEpmSXVJSMnr0aOzTOlAZmuC9sAKVyJRsIDs7+2c/+xm2KgVUg8q4PUNQiazJBnJzcydMmIDdWuDl5QUVoBpuyRZUIoOygRs3bsAc+xe/+AX27O09cuTIXr16XbhwAbdhDiqRTdlXrlwZPHhwXV3dqVOn4uPj32wBNuBhY2PjqlWrli5ditswB5XIpuxly5atXLkSR3/k2rVr/fr1KywsxAVsQSUyKLu4uLhv376Sv3UkEh4e/uKLL+IoW1CJDMr29/cPCgrC0dbAGb5///6s3k4RoBJZk11QUHD//fdXV1fjAkJMTMzTTz+NowxBJbIme+HChXCKxlEpbt++PWTIkNOnT+MCVqASmZKdl5fn7u5+/fp1XGCFbdu2zZw5E0dZgUpkSva8efM2btyIo9a5c+eOp6dnmz87bVCoRHZknzlzZuDAge39NldiYuK0adNwlAmoRHZk+/n5xcbG4mhbNDY2ent7nzx5EhcYHyqREdlwKh46dGhDQwMukEFSUtLjjz+Oo8aHSmRENtjasWMHjsoDDm4fH58TJ07gAoNDJbIg+7PPPnvooYfAGS6Qze7duydPnoyjBodKZEH2+PHj9+/fj6Pt4e7du8OHD4cXDS4wMlSi4WWnpKSMHDmyqakJF7STvXv3Tpw4EUeNDJVobNngeNSoUfRXwRXw/fff+/r6fvrpp7jAsFCJxpZ94MCBcePG4ahS4FowYcIEHDUsVKKBZQsX2oyMDFygFDi4H3nkkU8++QQXGBMq0cCyd+7cOXXqVBy1j4MHD6p4qnAuVKJRZTc0NHh6eqr+f49gEgDTvdTUVFxgQKhEo8qOj4+fM2cOjqrB4cOHR48ejaMGhEo0pOxbt24NGjTo66+/xgVqAAf3r371qw8//BAXGA0q0ZCyoXvPPPMMjqoHrOVgRWf/2t25UInGk11fX++Aj4/BNA0mazhqKKhE48mOjIycP38+jqpNenr6ww8/DKs7y2BxcbHlQ51DJRpAtuWHgmtqau6//37HfJ9j8uTJu3btsoz87W9/s3yoc6hEA8hetWqVuB0aGvrnP//ZolBDTp486eXldefOHTEye/Zsi3K9QyXqXTacSO+7776qqqrmH7/JUVRUhCtpxqxZs8Sj+bvvvuvSpQs6sesZKlHvsi9fvuzi4vL222/D9urVq19++WVcQ0tgdQdrPOFzbV999RX0xED/25ZK1LvsnJyce++996GHHqqoqOjbt6/43Xk41rds2XL8+PFWtTXg2WefXb9+PWxs3boVZINyXEOvUIl6lw1L3l69ej3wwAMw6K+99lpzy6V0wYIFIF6VdzYpdXV133zzjXipPn/+vPAVkxdeeAFk7927t3V1/UIl6l32u+++C7J9fHy6du0aHh4+fPhwGPHu3btr+qkSmIT36NHDw8NjypQpMCWcNm3an/70pyFDhsBTr127FtfWK1Si3mWvWbPGzc3N1dUVZA8YMOCee+4B93C04Xpqc/bs2UceeQTmhvC8gwcPhktJhw4dQLbD1gL2QyXqXfbixYthuF1agOF+8MEHhZm5A4B52R//+EcwLXYAmDFjBq6nV6hEvcv28/MTDqmOHTs+/vjjsP7BNTQGTum9e/eGk4oge9SoUbiGXqES9S77l7/8JQwxLHBh0eWsdybglO7r6+vp6Qk9gQMdF+sVKlHvsvv37w/HtLDOdiLCKb1nz54wN8RleoVKVF92Q0PDsWPHoqOjN23atNE+oqKiOnfuDKMsPIQdwm6PHDnS3m/vKQblMm/ePOgP9KpVLxXhgFyoRDVl19TUQA4wFl988cV1NSgtLT1+/DgKfvnll9C9iIiIgoIC3AP1sJYLPHt5ebllxB40zYVKVE324cOHIyMjQQ9OSBuuXbsGS3DwocWUjY1cqER1ZG/bti05ORknoT0XLlwICAiAQw13yA6YyYVKVEE2vCozMjJw3x1FRUXFG2+8odYYsZQLlWiv7Ozs7MTERNxrxwJjFBgYaP85kKVcmqUk2iW7sbEReob76wzgHCizz9ZgKRcBuhO7ZO/fvz8rKwt31kkkJCRcvHgRd1E2LOUiQCUql93U1BQaGoq76TxgTiu88awAlnIRoRKVyy4qKnL6FQ4RExOj7B4FS7mIUInKZe/cuTM/Px/30ank5OQo+0QDS7mIUInKZcN5BnfQ2dTX18vpOYWlXERoc+Wy161bhzvobOrq6uT0nMJSLiK0OVOyATk9p7CUiwhtzmX/AEu5iNDmXPYPsJSLCG3uaNmlpaX+/v4DBgzo1KmTt7d3SEhIdXW1ZYWysrLBgwdbRtqFnJ5TVM+lpqYmICAAirp16/bUU0+dP3++dVNZKMtFhDZ3tGzIfMOGDZcuXYJxgdXtsmXLXn/9dbEUhu+3v/2ti4vLTw3aiZyeU1TPBXYYGRlZXl5eUlLy17/+dfLkya1aykNZLiK0uaNld+3aFY5d8SG85Pv16yc+HD16dFZWllFk28hl2LBhFy5cELbBN9QUq8lHWS4itLmjZfv6+oaHh1v7XIDwRrJRZNvOReDbb7+FI37evHm4QAbKchGhzR0t+/PPP/f09ISL3PTp0+FZ0tPTcQ3jyG4zl9mzZ8M1u3fv3v/6179QkRyU5SJCmzta9vWWyUtGRkZYWNjUqVNdXV3p3SujyL4uIxe4nP/lL38ZMWIEistBWS4itLkTZFuSlpYGJ0MUNJBsSyRzAYqLizt37oyjMlCWiwht7mjZMIWprKwUH8J81c3NzaL8B4wi20YugwYNOnv2rLBdWFiobDGpLBcR2tzRslesWAELUJhyw9Dk5eXBBGfRokWojlFk28hl5cqVMC+7fPnylStXoFpQUFDrprJQlosIbe5o2XANCw4O9vb2hnmNu7s7jEhFRQWqYxTZNnKBIx4u1X369Bk4cCC8IKqqqlo3lYWyXERoc0fL1ho5PaewlIsIbc5l/wBLuYjQ5kzJVvweMEu5iNDmymXTNaXTUfzpDpZyEaHNlcuOiorCHXQ2N2/elNNzCku5iNDmymXv2bMnNzcX99GpZGdnHzt2DHdUBizlIkIlKpcNi8v4+HjcR6cC3Vb2xRmWchGhEpXLBkJCQmpra3E3nURZWVl0dDTuomxYykWASrRLdmZm5qFDh3BPncTGjRurq6txF2XDUi4CVKJdsoGwsLDi4mLcWYeTmpqanJyMO9dOWMqlWUqivbJhORgYGKjsdqBaHD9+PCEhAfes/bCUS7OURHtlN7f8f30YI2cdE0lJSTt37sR9UgpLuVCJKshubvndDjgHOviaB4uToKCgnJwc3Bv7YCYXKlEd2QJZWVmhoaFxcXGarlnz8/MTExPXrFkDPrT7V+8M5EIlqilboLKyct++fTCf3LBhQ5Q8XFxccEgK2CHsdvfu3aWlpfhZtUEyl4iICItOSbBu3bqOHTtGRkbiAgsckAuVqL5sBYBsHNIrsBbv37//7du3cUFrhg0b5oB/nWwbKlF6lGk9TTGQbJhAPf300zhKmDFjxscff4yjjoVKlB5lWk9TDCR72rRp//jHP3CU8Morr2zZsgVHHQuVKD3KtJ6mGEV2WVlZ79692zyHA2+//farr76Ko46FSpQeZVpPU4wiOzY2duHChTgqRWpq6qxZs3DUsVCJ0qNM62mKUWRPnz5d5o1MWFP5+PjgqGOhEqVHmdbTFEPIvnHjhpubW319PS6QoqGhoUuXLo2NjbjAgVCJ0qNM62mKIWR/+OGHMDvDUet4eXnZ/4/r7IFKlB5lWk9TDCF78eLFmzdvxlHrwCsjLS0NRx0IlSg9yrSephhCNlyD//vf/+KodZYsWRIXF4ejDoRKlB5lWk9T9C+7srISFl3t+kWamJiY5cuX46gDoRKlR5nW0xT9y4YLdnuXUikpKXPmzMFRB0IlSo8yracp+pcdEBAQERGBozY5d+7c8OHDcdSBUInSo0zraYr+ZU+ePDkjIwNHbeL039umEqVHmdbTFP3L7t+//5UrV3C0LZy7+qISpUeZ1tMUncuurq7u2bMnjsrAuasvKlF6lGk9TdG57MzMzHHjxuGoDJYuXRobG4ujjoJKlB5lWk9TdC47KSnpxRdfxFEZbN68WfiBd6dAJUqPMq2nKTqXHR4eHhoaiqMy+Oijj9q7YFMRKlF6lGk9TdG57OXLlyv75VfnvvdFJUqPMq2nKTqXvWDBgj179uCoDO7cuePE976oROlRpvU0Reey4VSs+ANlTvzkIZUoPcq0nqboXPb48eOzsrJwVB4zZsw4evQojjoEKlF6lGk9TRFku0gh1rlx44aHh8dPbRwIrLuys7OFbdy/FiCel5cHXmE53rdvX5i619TUCPWXLVv2zjvvKGtrJ1SivmTjgh+5fv36zJkzbVTQlIkTJ548eVLYttaHESNG7Nq1q76+vrq6GpZbcJkX4mAafAvb7W1rJ1Si9NPTeprSpuyxY8fm5ubaqKApU6dO/ec//ylsy+lDbW1tr169hG04h8NRK2y3t62dUInST0/raUqbsj/55JNmmxU0BWwJHWiW14erV696e3sL2zA7gzmasN3etnZCJUo/Pa2nKaJsCq3meH73u98dPnxY2Mb9a6F19eaEhATxJgysu2D1BWuw5va3tRMqET+ZAK2nKULONHNEmxU0Yvny5eKnz9rsQ3l5+ZNPPtnQ0CBGfHx88vPzmxW1tQcqUfrpaT1N0blsMC1+wMh2H8DTc889h94MhWX6Rx991KyorT1QidJPT+tpis5lJycnz507V9i20YeCgoL58+fDOgrFYYItnBgUtLUHKlH66Wk9TdG57DNnzowaNUrYttYHmMFBneLiYlzQ8qWhpUuXNitqaw9UovTT03qaIsqm0GqO59atW927dxd+zRr3rwWIDx06lAYF0tLShG8XoAoCttvaA5UovV9aT1PUSk87Jk2a9Omnn+KoPC5evOjl5YWj2kMlSo8yracp+pcNy6Hg4GAclcfdu3dh9WXn/6ZUAJUoPcq0nqboX/Znn302ceJEHJXN8OHDz507h6MaQyVKjzKtpyn6lw2X7R49esj8Cidlzpw5KSkpOKoxVKL0KNN6mqJ/2cDMmTM/+OADHJUHLNNjYmJwVGOoROlRpvU0xRCyt2zZsmjRIhyVR1xc3JIlS3BUY6hE6VGm9TTFELLhoqv4LQpx9eVIqETpUab1NMUQsoEBAwZcunQJR2XglNUXlSg9yrSephhF9nPPPZeUlGQZkflVLuG9L7Xe4ZAJlSg9yrSephhF9vbt29HHSOT/jIf43pdIu77arwAqUXqUaT11QXf8kWx13w9QkaKiIjiTf//992LkD3/4g0W5LWbNmpWamio+PHDggOK5vUyoROfIhnXIiRMnxIeWsiHu+FWKfMaMGSPeN4XT8n333Wft1lhdXZ1wO13g1VdfFb9p8J///KdPnz6KV+0yoRKdI7uyshKyFX2Lsg8dOiT8TvFPVXVGQkKCn5+fsC18LO6rr75qXeUnVq5cefbsWWEbVm6vvPIKbFRVVT3wwAPiB9O0g0p0jmxgwoQJXbt2FXwLshMTEzt16uTv74+r6onbt28PGTJE+GTxwYMHoedbt27FlX6ksLCwZ8+eQoWPP/4YBMOEbty4cffcc48D/rcOleg02V988QWMVPfu3cE3bIDpDh06wCg4/h5ye3nvvffGjx8PV+4VK1ZAn23faXnzzTchuyeffBJO3cOGDXvttdfgJe7m5va///0PV1UbKtFpsoHBgwd37twZzucwHDBqMF914ncerQGrJng5fvPNN8KHBoGmpqYpU6Zs3rz5scceg57//Oc/b92iFdDcw8MDXsRw6u7YsWO3bt0g5REjRuB6GkAlOlM2rGRgsOBlLhzTIFvxe8aaAlOtkJAQOAkNHTr00UcfFb5iP3DgQJheQP9BnrU5mkBWVhbUgZqurq7wF47voKAgXEkDqERnygZgBF1aGD16tGNe74opKSlZuHAhvCjhpXnvvfd26dIFtqHncMjamKMJzJ8/H5oImQ4aNEjxN8faBZXoZNlw2ROG4Ne//rWKv3KkHWfOnJk0aVLv3r0F04CXl5eNOZpAbW0tnMCE+nA+sFypaweV6GTZMLmFkxvMb+H4sH0y1BVHjx4Fxz169BDk2Z6jCezduxdO5u7u7i+99BIu0wYq0cmygblz58LMZe3atbhA38Aiatu2bXC8wot15MiRuFgKWHRBZa1vnIlQierLbmhoOHbsWHR09KZNmzbKAGYrsBoJCwvDBQTYIez2yJEjlnemnAvM3eBKBId4SkpKmykHBgZCphEREbjAOvakTCWqKbumpgY6FxUVBWto/HNlNklLS8Mh63z55ZfQPRiygoIC3AOHI6S8evVqmSmkp6fjkAyUpUwlqib78OHDkZGRpaWluKfacO3atXfffRcG2olXep2nTCWqIxuuXsnJybh32nPhwoWAgIDy8nLcIe3Rf8pUogqy4eWWkZGBO+UoKioq3njjDTnJq4ghUqYS7ZWdnZ2dmJiIu+NYIHmY+8g8udmPUVKmEu2S3djYCE+JO+IM4OQms892YqCUaaldsvfv35+VlYV74SQSEhIc8P+eDZQylahcdlNTU2hoKH5+5wGT1fXr1+NeqoqxUqYSlcsuKipy+qULERMTo+Dmg3yMlTKVqFz2zp078/Pz8ZM7lZycHE2/UmWslKlE5bLhBIKf2dnU19fL6blijJUyjSuXvW7dOvzMzqaurk5OzxVjrJRpnCnZgJyeK8ZYKdM4l90OjJUyjXPZ7cBYKdO4o2WXlpb6+/sPGDCgU6dO3t7eISEh1dXVQlF5efmiRYvc3d379Onz/PPPFxcXt24qCzk9V4zqKYsIn71EQZlYS5nGHS37qaee2rBhw6VLlyBhWLYuW7bs9ddfF4pgG6a7oBxKX3755WeffbZVS3nI6bliVE9ZIDc399FHH2VQdteuXcvKysSH58+f79evn7A9cODAK1euCNslJSU9e/YUq8lHTs8Vo3rKALwCHnvssczMTAZl+/r6hoeHt/mGf2FhoaenJ47KQE7PFaNFyqtXrxZ2y6Dszz//HCzC1Wv69OnwLNY+prNly5aAgAAclYGcnitG9ZRhe+rUqbW1tdeZlA3U1NRkZGSEhYVBnq6urvS2FJzoZs+effXqVRSXg5yeK0bdlOFYHzly5Llz54Q6bMq2JC0tDc5ylpGKigqY0Xz77beWQfnI6bli1E3597///Y4dO8Q4g7KFr1+LD2Hu7ebmJj68ePHiCy+8cPr0aTHSXuT0XDHqpix8QQTxUzPZWEuZxh0te8WKFXAxzsrKgpzz8vJg5gJra6EIXvJjx44VT2vKkNNzxaiesiXKTF+3njKNO1o2rDSCg4O9vb1hwuLu7g6LTjhvC0UeHh7avcxVQfWULVGW73XrKdO4o2VrjZyeK8ZYKdM4l90OjJUyjTMl28abu6pgrJRpXLlsuj52OjY+tqEKxkqZxpXLjoqKws/sbG7evCmn54oxVso0rlz2nj17cnNz8ZM7lezsbPn/X1IBxkqZSlQuG1aN8fHx+MmdCnTb9jdi7MRYKVOJymUDISEhwk18PVBWVhYdHY27qDYGSplKtEt2ZmbmoUOHcBecxMaNG6urq3EX1cZAKVOJdskGwsLClH1+SF1SU1OTk5Nx57TBKClTifbKhnVeYGBgVVUV7osDOX78eEJCAu6ZZhglZSrRXtkAvMwheWe92JOSkhz/D9QMkTKVqIJsAJb2cHJz8MUMVh1BQUE5OTm4Nw5B/ylTierIFsjKygoNDY2Li9N0MZqfn5+YmLhmzRoYaJk/0aEdek6ZSlRTtkBlZeW+fftgorhhw4YoVYEdwm53795dWlqKn9Wp6DNlKlFa9v79+4uKinCUYxy+/vpremdNWja8VHfs2IGjHOMQExND76xJywbCw8ObmppwlGMEamtrJe+sWZUNE7+jR4/iKMcIbNq0SfLOmlXZwNq1a2tqanCUo2/S09Ot3VmzJbuuri44OLixsREXcPQKLAVt3FmzJbu55VYR+ObHtyF4//33bd9Za0N2c8utIpis8eu3nsnNzQ0NDW3zzlrbsgVgvgbKt2/fztff+qG0tBSO5oiIiA8++EDOnTW5sgVg/X3gwIG3OPoATLfrzlr7ZHMMDZdtIrhsE8Flm4j/A2G9zW83FAaAAAAAAElFTkSuQmCC">some</a>
	 */
	@Disabled
	@Test
	public void testStateMachineVsStateMachineModelConsistency() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();

		try {
			// build statemachine from model
			UmlStateMachineModelFactory umlStateMachineModelFactory = new UmlStateMachineModelFactory(("classpath:org/springframework/statemachine/uml/simple-root-regions.uml"));
			StateMachineBuilder.Builder<String, String> stateMachineBuilder = StateMachineBuilder.builder();
			stateMachineBuilder.configureModel().withModel().factory(umlStateMachineModelFactory);
			stateMachineBuilder.configureConfiguration().withConfiguration();
			StateMachine<String, String> stateMachine = stateMachineBuilder.build();

			// get the "root" state of this state machines
			State<String, String> rootState = stateMachine.getStates().stream().findFirst().get();
			assertThat(rootState).isInstanceOf(RegionState.class);
			RegionState<String, String> rootRegionState = ((RegionState<String, String>) rootState);

			// compare statemachine and stateMachineModel

			// states in Region1
			AbstractStateMachine region1InStatemachine = (AbstractStateMachine)
					((List) rootRegionState.getRegions()).stream()
							.filter(region -> ((AbstractStateMachine) region).getId().contains("Region1"))
							.findFirst().get();

			List statesOfRegion1InStateMachine = region1InStatemachine.getStates().stream()
					.map(o -> ((State) o).getId().toString())
					.sorted().toList();

			List<String> statesOfRegion1InStateMachineModel = stateMachineModel.getStatesData().getStateData().stream()
					.filter(stateData -> "Region1".equals(stateData.getRegion().toString()))
					.map(stateData -> stateData.getState().toString())
					.sorted().toList();

			assertThat(statesOfRegion1InStateMachine).isEqualTo(statesOfRegion1InStateMachineModel);

			// states in Region2
			AbstractStateMachine region2InStatemachine = (AbstractStateMachine)
					((List) rootRegionState.getRegions()).stream()
							.filter(region -> ((AbstractStateMachine) region).getId().contains("Region2"))
							.findFirst().get();

			List statesOfRegion2InStateMachine = region2InStatemachine.getStates().stream()
					.map(o -> ((State) o).getId().toString())
					.sorted().toList();

			List<String> statesOfRegion2InStateMachineModel = stateMachineModel.getStatesData().getStateData().stream()
					.filter(stateData -> "Region2".equals(stateData.getRegion().toString()))
					.map(stateData -> stateData.getState().toString())
					.sorted().toList();

			assertThat(statesOfRegion2InStateMachine).isEqualTo(statesOfRegion2InStateMachineModel);

			// transitions in Region1
			List transitionsOfRegion1InStateMachine = region1InStatemachine.getTransitions().stream()
					.map(o -> ((Transition) o).getSource().getId().toString() + "->" + ((Transition) o).getTarget().getId().toString())
					.sorted().toList();

			List<String> transitionsOfRegion1InStateMachineModel = stateMachineModel.getTransitionsData().getTransitions().stream()
					// let's exclude "initial" transition
					.filter(transitionData -> !transitionData.getSource().startsWith("initial"))
					.filter(transitionData -> statesOfRegion1InStateMachine.contains(transitionData.getSource())
							|| statesOfRegion1InStateMachine.contains(transitionData.getTarget()))
					.map(transitionData -> transitionData.getSource() + "->" + transitionData.getTarget())
					.sorted().toList();

			assertThat(transitionsOfRegion1InStateMachine).isEqualTo(transitionsOfRegion1InStateMachineModel);

			// transitions in Region2
			List transitionsOfRegion2InStateMachine = region2InStatemachine.getTransitions().stream()
					.map(o -> ((Transition) o).getSource().getId().toString() + "->" + ((Transition) o).getTarget().getId().toString())
					.sorted().toList();

			List<String> transitionsOfRegion2InStateMachineModel = stateMachineModel.getTransitionsData().getTransitions().stream()
					// let's exclude "initial" transition
					.filter(transitionData -> !transitionData.getSource().startsWith("initial"))
					.filter(transitionData -> statesOfRegion2InStateMachine.contains(transitionData.getSource())
							|| statesOfRegion2InStateMachine.contains(transitionData.getTarget()))
					.map(transitionData -> transitionData.getSource() + "->" + transitionData.getTarget())
					.sorted().toList();

			// WOW! this is failing! Why is transition "S3->S4" present in both Region1 AND Region2 ?!?
			// Does this indicates an issue in UmlStateMachineModelFactory ???
			// Expected :["S1->S2"]
			// Actual   :["S1->S2", "S3->S4"]
			assertThat(transitionsOfRegion2InStateMachine).isEqualTo(transitionsOfRegion2InStateMachineModel);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSimpleFlatEnd() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat-end.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isEnd()).isTrue();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleEntryExit() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-entryexit.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.EXIT);
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size()).isEqualTo(1);
		assertThat(stateMachineModel.getTransitionsData().getExits().size()).isEqualTo(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleFlatMachine() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction action1 = context.getBean("action1", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(action1.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineMachine() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1", "S11");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1", "S12");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleRootRegionsMachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1", "S3");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S3");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleEntryExitMachine() throws Exception {
		context.register(Config5.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice1() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice2() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s3").build());
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice3() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMissingNameChoice() {
		context.register(Config6MissingName.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleForkJoin() {
		context.register(Config7.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("SF");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin1() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin2() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		stateMachine.getExtendedState().getVariables().put("foo", "bar");
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("SF");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryShallow() {
		context.register(Config8.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDeep() {
		context.register(Config9.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S211");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S212");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S212");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction1() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E4").setHeader("junction", "s5").build());
		assertThat(stateMachine.getState().getIds()).contains("S5");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction2() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E4").setHeader("junction", "s6").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction3() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S4");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S7");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleActions() throws Exception {
		context.register(Config11.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		TestExtendedStateListener listener = new TestExtendedStateListener();
		stateMachine.addStateListener(listener);
		LatchAction e1Action = context.getBean("e1Action", LatchAction.class);
		LatchAction s1Exit = context.getBean("s1Exit", LatchAction.class);
		LatchAction s2Entry = context.getBean("s2Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s1Exit.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s2Entry.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getExtendedState().getVariables().get("hellos2do")).isEqualTo("hellos2dovalue");
	}

	@Test
	public void testSimpleEventDefer() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-eventdefer.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getDeferred().size()).isEqualTo(1);
				assertThat(stateData.getDeferred().iterator().next()).isEqualTo("E2");
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getDeferred().size()).isEqualTo(0);
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getDeferred().size()).isEqualTo(0);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleTransitionTypes() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-transitiontypes.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitionDatas = stateMachineModel.getTransitionsData().getTransitions();
		assertThat(stateDatas.size()).isEqualTo(2);
		assertThat(transitionDatas.size()).isEqualTo(4);
		for (TransitionData<String, String> transitionData : transitionDatas) {
			if (transitionData.getEvent() != null) {
				if (transitionData.getEvent().equals("E1")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.EXTERNAL);
				} else if (transitionData.getEvent().equals("E2")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.LOCAL);
				} else if (transitionData.getEvent().equals("E3")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.INTERNAL);
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDefault() {
		context.register(Config12.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers1() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s3Entry = context.getBean("s3Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(s3Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers2() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s5Entry = context.getBean("s5Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(s5Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S5");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny1() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny2() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testInitialActions() throws Exception {
		context.register(Config15.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction initialAction = context.getBean("initialAction", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		assertThat(initialAction.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsAllow() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("foo", "bar").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(stateMachine.getExtendedState().get("myvar1", String.class)).isEqualTo("myvalue1");
		assertThat(stateMachine.getExtendedState().get("myvar2", String.class)).isEqualTo("myvalue2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsDeny() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1");
	}

	@Test
	public void testSimpleFlatMultipleToEnds() throws Exception {
		context.register(Config17.class);
		context.refresh();
	}

	@Test
	public void testSimpleFlatMultipleToEndsViachoices() throws Exception {
		context.register(Config18.class);
		context.refresh();
	}

	@Test
	public void testBrokenModelShadowEntries() throws Exception {
		context.register(Config19.class);
		context.refresh();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineRef() throws Exception {
		context.register(Config21.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S31");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleStateActions() throws Exception {
		context.register(Config22.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction e1Action = context.getBean("e1Action", LatchAction.class);
		LatchAction e2Action = context.getBean("e2Action", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(e2Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E20");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E30");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E21");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E31");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E22");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E32");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E21");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S22");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E23");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S22");
		assertThat(listener.entered).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E31");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S22");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E33");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S22");
		assertThat(listener.entered).contains("S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleConnectionPointRefMachine() throws Exception {
		context.register(Config24.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	public void testConnectionPointRef() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.EXIT);
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size()).isEqualTo(1);
		assertThat(stateMachineModel.getTransitionsData().getExits().size()).isEqualTo(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice1() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice2() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S4");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice3() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);
		LatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", LatchAction.class);
		LatchAction choiceToS5 = context.getBean("choiceToS5", LatchAction.class);
		LatchAction choiceToS6 = context.getBean("choiceToS6", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction1() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction2() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S4");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction3() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);
		LatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", LatchAction.class);
		LatchAction choiceToS5 = context.getBean("choiceToS5", LatchAction.class);
		LatchAction choiceToS6 = context.getBean("choiceToS6", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testPseudostateInSubmachineHaveCorrectParent() {
		context.refresh();
		Resource model = new ClassPathResource("org/springframework/statemachine/uml/pseudostate-in-submachine.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model);
		assertThat(model.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<String, String> choiceStateData = stateDatas.stream()
				.filter(sd -> "CHOICE".equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo("S1");
	}

	@Test
	public void testPseudostateInSubmachinerefHaveCorrectParent() {
		context.refresh();
		Resource model = new ClassPathResource("org/springframework/statemachine/uml/pseudostate-in-submachineref.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model);
		assertThat(model.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<String, String> choiceStateData = stateDatas.stream()
				.filter(sd -> "CHOICE".equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo("S1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTransitionEffectSpel() {
		context.register(Config27.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(stateMachine.getExtendedState().get("key", String.class)).isEqualTo("value");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportedSubMachine() {
		context.register(Config28.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("MAIN2", "CHILD2");
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public Action<String, String> action1() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-entryexit.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6MissingName extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/missingname-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config7 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-forkjoin.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config8 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-shallow.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config9 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-deep.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config10 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-junction.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public JunctionGuard s5Guard() {
			return new JunctionGuard("s5");
		}

		@Bean
		public JunctionGuard s6Guard() {
			return new JunctionGuard("s6");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config11 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-actions.uml");
		}

		@Bean
		public LatchAction s1Exit() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s2Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config12 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-history-default.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config13 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-timers.uml");
		}

		@Bean
		public LatchAction s3Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s5Entry() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config14 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-guards.uml");
		}

		@Bean
		public SimpleGuard denyGuard() {
			return new SimpleGuard(false);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config15 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/initial-actions.uml");
		}

		@Bean
		public LatchAction initialAction() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config16 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-spels.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config17 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config18 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end-viachoices.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config19 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/broken-model-shadowentries.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config20 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/multijoin-forkjoin.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config21 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-submachineref.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config22 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-state-actions.uml");
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e2Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config23 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-localtransition.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config24 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config25 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config26 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-junction.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config27 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/transition-effect-spel.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config28 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource mainModel = new ClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
			Resource subModel = new ClassPathResource("org/springframework/statemachine/uml/import-sub/import-sub.uml");
			return new UmlStateMachineModelFactory(mainModel, new Resource[] { subModel });
		}
	}

	public static class LatchAction implements Action<String, String> {
		CountDownLatch latch = new CountDownLatch(1);
		@Override
		public void execute(StateContext<String, String> context) {
			latch.countDown();
		}
	}

	private static class ChoiceGuard implements Guard<String, String> {

		private final String match;

		public ChoiceGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("choice", String.class));
		}
	}

	private static class SimpleGuard implements Guard<String, String> {

		private final boolean deny;

		public SimpleGuard(boolean deny) {
			this.deny = deny;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return deny;
		}
	}


	private static class JunctionGuard implements Guard<String, String> {

		private final String match;

		public JunctionGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("junction", String.class));
		}
	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		final ArrayList<String> entered = new ArrayList<>();
		final ArrayList<String> exited = new ArrayList<>();

		@Override
		public void stateEntered(State<String, String> state) {
			entered.add(state.getId());
		}

		@Override
		public void stateExited(State<String, String> state) {
			exited.add(state.getId());
		}

		public void reset() {
			entered.clear();
			exited.clear();
		}
	}

	private static class TestExtendedStateListener extends StateMachineListenerAdapter<String, String> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void extendedStateChanged(Object key, Object value) {
			if (ObjectUtils.nullSafeEquals(key, "hellos2do")) {
				latch.countDown();
			}
		}
	}
}
